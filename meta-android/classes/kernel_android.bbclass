#
# This class is used to create Android device compatible kernel images
#

ANDROID_BOOTIMG_CMDLINE ?= ""
ANDROID_BOOTIMG_KERNEL_RAM_BASE ?= "Please set to right value!"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS ?= ""

# Boot image header version.
#
# 0 keeps the historical abootimg path, which is all abootimg can produce: it
# predates header versions entirely and always writes a v0 header with 2048
# byte pages. Every device here booted that way until Android 10.
#
# From Android 11 the bootloader wants a v2 header with 4096 byte pages and the
# device tree in its own section rather than concatenated onto the kernel. A
# Pixel 3a flashed with stock Android 11 refuses a v0 image outright. Devices
# that need it set ANDROID_BOOTIMG_HEADER_VERSION = "2"; the rest are untouched.
ANDROID_BOOTIMG_HEADER_VERSION ?= "0"
ANDROID_BOOTIMG_PAGESIZE ?= "2048"
ANDROID_BOOTIMG_DTB_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_OS_VERSION ?= "0"

# Where the device tree in the v2 dtb section comes from. Empty means the one
# this kernel builds (KERNEL_DEVICETREE); a path names a prebuilt blob instead,
# for a device whose bootloader will not accept the kernel's own.
ANDROID_BOOTIMG_DTB ?= ""

KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

INITRAMFS_NAME = "initramfs-android-image-${MACHINE}.cpio.gz"

def android_bootimg_v2(d, kernel, ramdisk, dtb, out):
    """Assemble a v1/v2 Android boot image.

    abootimg cannot do this and meta-oe's mkbootimg is the pre-header-version
    AOSP one, so neither tool in the tree can produce it. The format is small
    and stable enough to write directly; switching to AOSP's mkbootimg.py would
    be the tidier answer if this ever needs v3/v4 as well.
    """
    import struct, hashlib

    page = int(d.getVar("ANDROID_BOOTIMG_PAGESIZE"))
    hv = int(d.getVar("ANDROID_BOOTIMG_HEADER_VERSION"))
    base = lambda v: int(d.getVar(v), 0)

    read = lambda p: open(p, "rb").read() if p else b""
    k, r, t = read(kernel), read(ramdisk), read(dtb)
    second = b""
    recovery_dtbo = b""

    # header_size is the size of the header struct itself, not a page
    hdr_size = 1648 if hv == 1 else 1660

    sha = hashlib.sha1()
    for part in (k, r, second, recovery_dtbo) + ((t,) if hv >= 2 else ()):
        sha.update(part)
        sha.update(struct.pack("<I", len(part)))

    h = struct.pack("<8sIIIIIIIIII", b"ANDROID!",
                    len(k), base("ANDROID_BOOTIMG_KERNEL_RAM_BASE"),
                    len(r), base("ANDROID_BOOTIMG_RAMDISK_RAM_BASE"),
                    len(second), base("ANDROID_BOOTIMG_SECOND_RAM_BASE"),
                    base("ANDROID_BOOTIMG_TAGS_RAM_BASE"),
                    page, hv, base("ANDROID_BOOTIMG_OS_VERSION"))
    cmdline = (d.getVar("ANDROID_BOOTIMG_CMDLINE") or "").encode()
    if len(cmdline) > 1535:
        bb.fatal("ANDROID_BOOTIMG_CMDLINE is %d bytes, the header holds 1535" % len(cmdline))
    h += struct.pack("<16s", b"")
    h += struct.pack("<512s", cmdline[:512])
    h += struct.pack("<32s", sha.digest() + b"\0" * 12)
    h += struct.pack("<1024s", cmdline[512:])
    h += struct.pack("<IQI", len(recovery_dtbo), 0, hdr_size)
    if hv >= 2:
        h += struct.pack("<IQ", len(t), base("ANDROID_BOOTIMG_DTB_RAM_BASE"))
    if len(h) != hdr_size:
        bb.fatal("assembled a %d byte header, expected %d" % (len(h), hdr_size))

    pad = lambda b: b + b"\0" * (-len(b) % page)
    with open(out, "wb") as f:
        for part in (h, k, r, second, recovery_dtbo) + ((t,) if hv >= 2 else ()):
            f.write(pad(part))

do_deploy[depends] += "initramfs-android-image:do_image_complete abootimg-native:do_populate_sysroot"

# A postfunc rather than do_deploy:append. do_deploy is a shell task, so
# appending python to it makes bitbake lex the python as shell and the recipe
# fails to parse; postfuncs may be python regardless of the task's own type.
do_deploy[postfuncs] += "android_bootimg_deploy"

python android_bootimg_deploy() {
    import os, shutil

    b = d.getVar("B")
    deploy_image = d.getVar("DEPLOY_DIR_IMAGE")
    initramfs = os.path.join(deploy_image, d.getVar("INITRAMFS_NAME"))
    if not os.path.exists(initramfs):
        bb.fatal("Required initramfs image %s is not available!" % initramfs)

    kernel = os.path.join(b, d.getVar("KERNEL_OUTPUT"))
    bootimg = os.path.join(b, "boot.img")

    dtb = d.getVar("ANDROID_BOOTIMG_DTB") or ""
    if dtb:
        bb.note("Using prebuilt device tree %s rather than the kernel's own" % dtb)
    elif d.getVar("KERNEL_DEVICETREE"):
        dtb = os.path.join(b, d.getVar("KERNEL_OUTPUT_DIR"), "dts",
                           d.getVar("KERNEL_DEVICETREE").strip())
    if dtb and not os.path.exists(dtb):
        bb.fatal("Device tree %s does not exist" % dtb)

    # Several machines here set KERNEL_IMAGETYPE = "Image.gz-dtb" and leave
    # KERNEL_DEVICETREE unset, because the kernel's own build target already
    # concatenates the device tree onto the compressed image. That is the v0
    # arrangement. A v2 header wants the tree in its own section instead, so
    # split it back off rather than making every such machine restate its dts.
    if d.getVar("ANDROID_BOOTIMG_HEADER_VERSION") != "0" and not dtb:
        with open(kernel, "rb") as f:
            blob = f.read()
        fdt = blob.find(b"\xd0\x0d\xfe\xed")
        if fdt < 0:
            bb.fatal("Header version %s needs a device tree, but %s has no "
                     "appended FDT and neither ANDROID_BOOTIMG_DTB nor "
                     "KERNEL_DEVICETREE is set"
                     % (d.getVar("ANDROID_BOOTIMG_HEADER_VERSION"), kernel))
        kernel = os.path.join(b, "kernel-without-appended-dtb")
        dtb = os.path.join(b, "appended-dtb")
        with open(kernel, "wb") as f:
            f.write(blob[:fdt])
        with open(dtb, "wb") as f:
            f.write(blob[fdt:])
        bb.note("Split the appended device tree off the kernel: %d byte kernel, "
                "%d byte dtb" % (fdt, len(blob) - fdt))

    if d.getVar("ANDROID_BOOTIMG_HEADER_VERSION") == "0":
        # v0 has nowhere to put a device tree, so it is concatenated onto the
        # kernel and the bootloader is expected to find it there.
        kernel_with_dtb = kernel
        if dtb:
            kernel_with_dtb = os.path.join(b, d.getVar("KERNEL_OUTPUT_DIR"),
                                           d.getVar("KERNEL_IMAGETYPE") + "-dtb")
            bb.note("DTB kernel: creating %s from %s and %s" % (kernel_with_dtb, kernel, dtb))
            with open(kernel_with_dtb, "wb") as out:
                for part in (kernel, dtb):
                    with open(part, "rb") as f:
                        shutil.copyfileobj(f, out)
        cmd = ["abootimg", "--create", bootimg, "-k", kernel_with_dtb, "-r", initramfs]
        for key, var in (("cmdline",    "ANDROID_BOOTIMG_CMDLINE"),
                         ("kerneladdr", "ANDROID_BOOTIMG_KERNEL_RAM_BASE"),
                         ("ramdiskaddr","ANDROID_BOOTIMG_RAMDISK_RAM_BASE"),
                         ("secondaddr", "ANDROID_BOOTIMG_SECOND_RAM_BASE"),
                         ("tagsaddr",   "ANDROID_BOOTIMG_TAGS_RAM_BASE")):
            cmd += ["-c", "%s=%s" % (key, d.getVar(var))]
        cmd += (d.getVar("ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS") or "").split()
        bb.process.run(cmd)
    else:
        bb.note("Assembling a header version %s boot image, page size %s" %
                (d.getVar("ANDROID_BOOTIMG_HEADER_VERSION"),
                 d.getVar("ANDROID_BOOTIMG_PAGESIZE")))
        android_bootimg_v2(d, kernel, initramfs, dtb or None, bootimg)

    deploydir = d.getVar("DEPLOYDIR")
    name = d.getVar("KERNEL_IMAGE_NAME")
    link = d.getVar("KERNEL_IMAGE_LINK_NAME")
    for t in d.getVar("KERNEL_IMAGETYPES").split():
        target = os.path.join(deploydir, "%s-%s.fastboot" % (t, name))
        shutil.copyfile(bootimg, target)
        for alias in ("%s-%s.fastboot" % (t, link), "%s.fastboot" % t):
            dst = os.path.join(deploydir, alias)
            if os.path.lexists(dst):
                os.remove(dst)
            os.symlink(os.path.basename(target), dst)
}
