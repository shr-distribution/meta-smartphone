SUMMARY = "Initramfs image to boot from internal flash of several Android based devices"
DESCRIPTION = "Provides a minimal environment to bootstrap and run a linux system on a \
Android based device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL ?= ""

VIRTUAL-RUNTIME_android-initramfs-scripts ?= "initramfs-scripts-android"

# The debug shell (INITRAMFS_DEBUG_SHELL, meta-android/conf/layer.conf) starts
# adbd-initramfs: root, and any host is let in. Without the shell it is not
# built, nor installed here. Not android-tools, which has the libraries but no
# adbd (that is android-tools-adbd, the rootfs's, which asks a service the
# initramfs does not have) and brings 7-Zip.
#
# A machine with no room for it sets ANDROID_INITRAMFS_DEBUG_ADBD = "0" and
# keeps telnet over USB networking. It adds 5.2 MB to the compressed ramdisk
# on aarch64 and 4.5 MB on armv7, so it is off by default where the ramdisk
# has its own init_boot partition (ANDROID_BOOTIMG_INIT_BOOT = "1": lagos,
# brax3, panther, tangorpro): those are 8 MiB, and brax3's 4.7 MB ramdisk
# would become 9.3 MB.
ANDROID_INITRAMFS_DEBUG_ADBD ?= "${@'0' if d.getVar('ANDROID_BOOTIMG_INIT_BOOT') == '1' else '1'}"

def android_initramfs_adbd(d):
    if d.getVar('INITRAMFS_DEBUG_SHELL') != '1':
        return ''
    if d.getVar('ANDROID_INITRAMFS_DEBUG_ADBD') != '1':
        return ''
    return 'android-tools-adbd-initramfs'

IMAGE_INSTALL = "busybox busybox-mdev base-passwd bash"
IMAGE_INSTALL += "${@android_initramfs_adbd(d)}"
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

# A machine that boots the generic rootfs (HALIUM_GENERIC_ROOTFS_DEVICE = "1")
# builds no rootfs of its own, so MACHINE_EXTRA_RDEPENDS never reaches a device.
# An out-of-tree module named there is silently absent from every install: that
# is how sargo, sunfish, surya and fajita lost v4l2loopback. Such a module goes
# in ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL, which ships it next to its kernel.
def android_initramfs_lost_modules(d):
    wanted = (d.getVar("MACHINE_EXTRA_RDEPENDS") or "").split()
    wanted += (d.getVar("MACHINE_EXTRA_RRECOMMENDS") or "").split()
    return [p for p in wanted
            if p in ("v4l2loopback", "wireguard-module") or p.startswith("kernel-module-")]

python () {
    if d.getVar("HALIUM_GENERIC_ROOTFS_DEVICE") == "1":
        lost = android_initramfs_lost_modules(d)
        if lost:
            bb.fatal("%s boots the generic rootfs, so %s in MACHINE_EXTRA_RDEPENDS "
                     "would never be installed. Add it to "
                     "ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL instead."
                     % (d.getVar("MACHINE"), " ".join(lost)))

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
