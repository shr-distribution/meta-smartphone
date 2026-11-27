# SPDX-License-Identifier: MIT
#
# An unsigned AVB hash footer for a boot image - what `avbtool add_hash_footer
# --partition_name boot --partition_size N` writes when it is given no key.
#
# Some vendors chain `boot` from the top-level vbmeta (a chain descriptor with
# its own rollback location) instead of describing it with a hash descriptor,
# and their boot images carry a footer and vbmeta struct of their own. A boot
# image written by kernel_android.bbclass has neither. libavb itself skips
# chained partitions once the top-level vbmeta has verification disabled, but
# a bootloader may read the boot footer on its own account, outside libavb,
# and that is not worth finding out on hardware: a device whose stock boot
# image has one gets one here too. UBports' build tools do the same on the same devices.
#
# The footer is unsigned (algorithm NONE): it satisfies a parser, not a
# signature check, so verification still has to be disabled in the top-level
# vbmeta (flags 2 or 3).
#
# Its own vbmeta struct says so too (flags 2, verification disabled), because
# on some devices it *is* the top-level vbmeta. The original Unihertz Titan
# (MT6771, Android 10) has no vbmeta partition: lk takes the top-level vbmeta
# from boot's footer, and stock's lists hash descriptors for boot, dtbo and
# recovery. Ours listed only boot, with flags 0, so a recovery boot (Volume Up)
# found no descriptor for "recovery" and lk hung on the logo until its
# watchdog reset the phone - and fastboot, reached from recovery's menu, went
# with it. With flags 2 libavb loads a requested partition without one.
# Measured 15 Nov 2026: recovery came up as soon as only this word changed.
# libavb reads the flags of the top-level vbmeta only, so where boot is
# chained from a vbmeta partition (Titan Pocket, Titan Slim) they change
# nothing.
#
# The layout is external/avb/libavb/avb_footer.h, avb_vbmeta_image.h and
# avb_hash_descriptor.h, all big-endian. Byte-identical to avbtool 1.3.0
# (external/avb main) run with --salt set to the image's SHA-256 and --flags 2.

import hashlib
import struct

BLOCK_SIZE = 4096
FOOTER_SIZE = 64
RELEASE_STRING = b"avbtool 1.3.0"
AVB_VBMETA_IMAGE_FLAGS_VERIFICATION_DISABLED = 2


def _hash_descriptor(partition_name, image_size, salt, digest):
    name = partition_name.encode()
    body = struct.pack(">Q32sIIII60x", image_size, b"sha256",
                       len(name), len(salt), len(digest), 0)
    body += name + salt + digest
    # num_bytes_following counts the padding that makes the whole descriptor,
    # 16-byte tag/length prefix included, a multiple of 8.
    body += b"\0" * (-(16 + len(body)) % 8)
    return struct.pack(">QQ", 2, len(body)) + body


def _vbmeta_unsigned(descriptors):
    aux = descriptors + b"\0" * (-len(descriptors) % 64)
    header = struct.pack(
        ">4sIIQQI"         # magic, libavb 1.0, auth size 0, aux size, algorithm NONE
        "QQQQQQQQ"         # hash, signature, public key, key metadata: all empty
        "QQ"               # descriptors offset/size within aux
        "QII"              # rollback index, flags, rollback index location
        "48s80x",          # release string, reserved
        b"AVB0", 1, 0, 0, len(aux), 0,
        # The empty key and key metadata still get an offset, just past the
        # descriptors, because that is where avbtool puts them.
        0, 0, 0, 0, len(descriptors), 0, len(descriptors), 0,
        0, len(descriptors),
        0, AVB_VBMETA_IMAGE_FLAGS_VERIFICATION_DISABLED, 0,
        RELEASE_STRING)
    assert len(header) == 256
    return header + aux


def add_hash_footer(path, partition_name, partition_size):
    """Turn the image at path into a partition_size image with a footer.

    The salt is the image's own SHA-256 rather than random bytes, so the same
    input always produces the same output.
    """
    with open(path, "rb") as f:
        image = f.read()
    original_size = len(image)

    salt = hashlib.sha256(image).digest()
    digest = hashlib.sha256(salt + image).digest()
    vbmeta = _vbmeta_unsigned(
        _hash_descriptor(partition_name, original_size, salt, digest))

    vbmeta_offset = original_size + (-original_size % BLOCK_SIZE)
    padded_vbmeta = vbmeta + b"\0" * (-len(vbmeta) % BLOCK_SIZE)
    needed = vbmeta_offset + len(padded_vbmeta) + BLOCK_SIZE
    if needed > partition_size:
        raise ValueError("%s is %d bytes; with its AVB footer it needs %d, but "
                         "the %s partition is %d" % (path, original_size, needed,
                                                     partition_name, partition_size))

    footer = struct.pack(">4sIIQQQ28x", b"AVBf", 1, 0,
                         original_size, vbmeta_offset, len(vbmeta))
    with open(path, "wb") as f:
        f.write(image)
        f.write(b"\0" * (vbmeta_offset - original_size))
        f.write(padded_vbmeta)
        f.truncate(partition_size - FOOTER_SIZE)
        f.seek(partition_size - FOOTER_SIZE)
        f.write(footer)
