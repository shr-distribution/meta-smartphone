SUMMARY = "Initramfs image to boot from internal flash of several Android based devices"
DESCRIPTION = "Provides a minimal environment to bootstrap and run a linux system on a \
Android based device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL ?= ""

VIRTUAL-RUNTIME_android-initramfs-scripts ?= "initramfs-scripts-android"

IMAGE_INSTALL = "busybox busybox-mdev base-passwd android-tools bash"
IMAGE_INSTALL += "${VIRTUAL-RUNTIME_android-initramfs-scripts}"
IMAGE_INSTALL += "${ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL}"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
export IMAGE_BASENAME = "initramfs-android-image"
IMAGE_NAME_SUFFIX ?= ""
IMAGE_LINGUAS = ""

# NOTE we must use cpio.gz here as this is what mkbootimg requires
IMAGE_FSTYPES:forcevariable = "cpio.gz"

# We don't need depmod data here
KERNELDEPMODDEPEND = ""
USE_DEPMOD = "0"

inherit core-image nopackages

# Unpack the machine's kernel modules into the initramfs, from the tarball the
# kernel recipe deploys. Opt in per machine with
# ANDROID_INITRAMFS_KERNEL_MODULES = "1".
#
# A device whose vendor loads kernel modules can only use modules built against
# the kernel it boots, and the surest way to keep the two together is to ship
# them in the same image. On bramble they must also be *here* rather than in the
# rootfs for a harder reason: its storage drivers are modules, so they have to
# be present before anything is mounted.
#
# Two routes exist because the recipes differ, not by preference:
#   - a kernel recipe using kernel.bbclass (sunfish) can install the
#     kernel-modules package, with KERNEL_SPLIT_MODULES = "0" so that LTO's
#     "depends=foo.lto" fields do not become unsatisfiable RDEPENDS;
#   - a standalone recipe (bramble) has no such packaging, so it deploys a
#     tarball, which is what this unpacks.
# The tarball route is only safe where the boot image is assembled by a
# *different* recipe. When the kernel's own do_deploy builds it, depending on
# that task is a dependency loop.
ANDROID_INITRAMFS_KERNEL_MODULES ?= "0"

# Which recipe actually deploys the tarball. NOT always virtual/kernel: on a
# GKI-style machine that is linux-dummy, because the boot image is assembled
# from a separately built kernel named by GKI_KERNEL_PROVIDER. Depending on
# virtual/kernel there makes do_rootfs run before the real kernel has deployed
# anything, and the check below then fails with the tarball missing - which is
# exactly what bramble did.
ANDROID_INITRAMFS_KERNEL_MODULES_PROVIDER ?= "${@d.getVar('GKI_KERNEL_PROVIDER') or 'virtual/kernel'}"

python () {
    if d.getVar("ANDROID_INITRAMFS_KERNEL_MODULES") == "1":
        provider = d.getVar("ANDROID_INITRAMFS_KERNEL_MODULES_PROVIDER")
        if not provider:
            bb.fatal("ANDROID_INITRAMFS_KERNEL_MODULES is set but no provider "
                     "resolved - set ANDROID_INITRAMFS_KERNEL_MODULES_PROVIDER "
                     "or GKI_KERNEL_PROVIDER in the machine conf")
        d.appendVarFlag("do_rootfs", "depends", " %s:do_deploy" % provider)
        d.appendVar("ROOTFS_POSTPROCESS_COMMAND", " android_initramfs_add_modules;")
}

android_initramfs_add_modules() {
    tarball="${DEPLOY_DIR_IMAGE}/modules-${MACHINE}.tgz"
    if [ ! -f "$tarball" ]; then
        bbfatal "ANDROID_INITRAMFS_KERNEL_MODULES is set but $tarball does not exist." \
                "Is ANDROID_INITRAMFS_KERNEL_MODULES_PROVIDER (${ANDROID_INITRAMFS_KERNEL_MODULES_PROVIDER})" \
                "the recipe that deploys it?"
    fi
    tar -xzf "$tarball" -C ${IMAGE_ROOTFS}
    bbnote "initramfs: added $(find ${IMAGE_ROOTFS}/lib/modules -name '*.ko' | wc -l) kernel modules"
}
