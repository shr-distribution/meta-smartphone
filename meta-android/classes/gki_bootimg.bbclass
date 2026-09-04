# Boot images for a GKI device, from a kernel this build did not produce.
#
# kernel_android.bbclass assembles the boot image inside the device's kernel
# recipe, because on a Tier B device the kernel *is* built here. A GKI device is
# the other case: the kernel is the Android Common Kernel, and it has to be
# built by Google's own Kleaf/Bazel tooling with the clang the KMI was frozen
# with. Rebuilding it with OE's cross toolchain would change the exported symbol
# CRCs under CONFIG_MODVERSIONS and the device's own stock vendor modules would
# then refuse to load - which is the whole point of the Tier A arrangement. So
# the kernel arrives as a prebuilt Image, and this class only packs it.
#
# What it produces, into DEPLOY_DIR_IMAGE:
#
#   boot-${MACHINE}-luneos.img        the kernel, plus the ramdisk unless the
#                                     device has an init_boot partition
#   init_boot-${MACHINE}-luneos.img   the ramdisk, only when it has one
#   boot-${MACHINE}-luneos-debug.img  self-contained kernel+ramdisk carrying
#                                     ANDROID_BOOTIMG_DEBUG_CMDLINE, for
#                                     "fastboot boot" - never flashed
#
# Set in the machine configuration:
#
#   GKI_KERNEL_IMAGE            path to the prebuilt Image (see below)
#   ANDROID_BOOTIMG_INIT_BOOT   "1" on an Android-13-launch device
#   ANDROID_BOOTIMG_CMDLINE     usually empty: a GKI device's command line comes
#                               from vendor_boot and the bootloader

inherit deploy

# Where the prebuilt kernel comes from. There is deliberately no default: it is
# built outside this tree by luneos-bootimg-<device>'s build-bootimg.sh (ACK at
# the pinned tag + luneos_defconfig, via Kleaf), so pointing at it is a local
# decision. Put it in local.conf, e.g.
#
#   GKI_KERNEL_IMAGE = "/media/herrie/LuneOS/bluejay-kernel/out-luneos-tierA/Image.lz4"
GKI_KERNEL_IMAGE ?= ""

ANDROID_BOOTIMG_CMDLINE ?= ""
ANDROID_BOOTIMG_HEADER_VERSION ?= "4"
ANDROID_BOOTIMG_OS_VERSION ?= "0"
ANDROID_BOOTIMG_INIT_BOOT ?= "0"
ANDROID_BOOTIMG_DEBUG_CMDLINE ?= "enable_adb"

INITRAMFS_NAME ?= "initramfs-android-image-${MACHINE}.cpio.gz"

# The kernel lives outside the build directory, so bitbake cannot see it change
# on its own. Hashing the file itself is what makes a new kernel rebuild the
# images instead of returning a stale sstate result.
do_deploy[file-checksums] += "${GKI_KERNEL_IMAGE}:True"
do_deploy[depends] += "initramfs-android-image:do_image_complete"

python do_deploy() {
    import os
    from halium.bootimg import write_bootimg_v3

    kernel = d.getVar("GKI_KERNEL_IMAGE")
    if not kernel:
        bb.fatal("GKI_KERNEL_IMAGE is not set. This machine's kernel is the "
                 "Android Common Kernel and is built outside this tree - see "
                 "the luneos-bootimg-<device> repository - so point this at the "
                 "Image it produced, in local.conf.")
    if not os.path.exists(kernel):
        bb.fatal("GKI_KERNEL_IMAGE %s does not exist" % kernel)

    initramfs = os.path.join(d.getVar("DEPLOY_DIR_IMAGE"), d.getVar("INITRAMFS_NAME"))
    if not os.path.exists(initramfs):
        bb.fatal("Required initramfs image %s is not available!" % initramfs)

    hv = int(d.getVar("ANDROID_BOOTIMG_HEADER_VERSION"))
    osver = int(d.getVar("ANDROID_BOOTIMG_OS_VERSION"), 0)
    cmdline = d.getVar("ANDROID_BOOTIMG_CMDLINE") or ""
    machine = d.getVar("MACHINE")
    out = d.getVar("DEPLOYDIR")
    bb.utils.mkdirhier(out)

    def pack(name, **kw):
        path = os.path.join(out, name)
        write_bootimg_v3(path, header_version=hv, os_version=osver, **kw)
        bb.note("packed %s (%d bytes)" % (name, os.path.getsize(path)))

    if d.getVar("ANDROID_BOOTIMG_INIT_BOOT") == "1":
        # Android-13-launch layout: the kernel is alone in boot and the generic
        # ramdisk has its own init_boot partition. The bootloader merges
        # vendor_kernel_boot and vendor_boot underneath it, so the vendor
        # modules are still at /lib/modules for the initramfs to load.
        pack("boot-%s-luneos.img" % machine, kernel=kernel, cmdline=cmdline)
        pack("init_boot-%s-luneos.img" % machine, ramdisk=initramfs)
    else:
        pack("boot-%s-luneos.img" % machine, kernel=kernel, ramdisk=initramfs,
             cmdline=cmdline)

    # The debug image is always self-contained, even on an init_boot device:
    # it is booted out of RAM with "fastboot boot", where relying on whatever
    # init_boot currently holds would defeat the point of having it.
    debug = d.getVar("ANDROID_BOOTIMG_DEBUG_CMDLINE")
    if debug:
        pack("boot-%s-luneos-debug.img" % machine, kernel=kernel,
             ramdisk=initramfs, cmdline=(cmdline + " " + debug).strip())
}

addtask deploy before do_build after do_install
