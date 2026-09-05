# SPDX-License-Identifier: MIT
#
# Android boot image header version 3 and 4.
#
# kernel_android.bbclass already writes v0 (through abootimg) and v1/v2 (by
# hand), and says in a comment that reaching for AOSP's mkbootimg.py would be
# the tidier answer if v3/v4 were ever needed. It is now - GKI devices are all
# v4 - but the format is even simpler than v2, so it lives here instead, in one
# place both kernel_android.bbclass and gki_bootimg.bbclass can import.
#
# The layout is system/tools/mkbootimg/include/bootimg/bootimg.h:
#
#   magic[8] "ANDROID!"
#   u32 kernel_size, ramdisk_size
#   u32 os_version        (os_version << 11) | os_patch_level
#   u32 header_size       1580 for v3, 1584 for v4
#   u32 reserved[4]
#   u32 header_version
#   u8  cmdline[512 + 1024]
#   u32 signature_size    v4 only; 0 unless the image is GKI-signed
#
# then the header padded to 4096, the kernel padded to 4096 and the ramdisk
# padded to 4096. Unlike v0-v2 the page size is fixed at 4096 and is not a
# header field, and there is no second, recovery_dtbo or dtb section: a v3+
# device carries its device tree in vendor_boot or dtbo.
#
# Verified byte-for-byte against AOSP mkbootimg.py output for both shapes we
# use: kernel+ramdisk (bluejay's boot.img) and each half on its own (panther,
# where the kernel goes to boot and the ramdisk to init_boot).

import struct

BOOT_MAGIC = b"ANDROID!"
PAGE_SIZE = 4096
HEADER_SIZE = {3: 1580, 4: 1584}
CMDLINE_SIZE = 512 + 1024


def _pad(blob):
    return blob + b"\0" * (-len(blob) % PAGE_SIZE)


def write_bootimg_v3(out, kernel=None, ramdisk=None, header_version=4,
                     cmdline="", os_version=0):
    """Assemble a v3/v4 boot image at path `out`.

    kernel and ramdisk are paths, and either may be None: a GKI device with an
    init_boot partition wants a kernel-only boot.img and a ramdisk-only
    init_boot.img, and the header records a zero size for the missing half.
    """
    if header_version not in HEADER_SIZE:
        raise ValueError("header version %r is not 3 or 4" % header_version)

    read = lambda p: open(p, "rb").read() if p else b""
    k, r = read(kernel), read(ramdisk)

    if isinstance(cmdline, str):
        cmdline = cmdline.encode()
    if len(cmdline) > CMDLINE_SIZE - 1:
        raise ValueError("cmdline is %d bytes, the header holds %d"
                         % (len(cmdline), CMDLINE_SIZE - 1))

    header_size = HEADER_SIZE[header_version]
    h = struct.pack("<8sIIII4II",
                    BOOT_MAGIC, len(k), len(r), os_version, header_size,
                    0, 0, 0, 0, header_version)
    h += struct.pack("<%ds" % CMDLINE_SIZE, cmdline)
    if header_version >= 4:
        # boot signature size; only set for legacy GKI-signed images
        h += struct.pack("<I", 0)
    if len(h) != header_size:
        raise ValueError("assembled a %d byte header, expected %d"
                         % (len(h), header_size))

    with open(out, "wb") as f:
        for part in (h, k, r):
            f.write(_pad(part))
