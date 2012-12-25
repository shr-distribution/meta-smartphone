require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "grouper"

do_deploy[depends] += "chroot-image:do_build"
DEPENDS += "android-image-utils-native chroot-image"
DESCRIPTION = "Linux kernel for the Asus Grouper device"

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=grouper/3.1/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "11ff94d0292b6b855324151435f61ed9921bc978"

KV = "3.1.10"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"

do_deploy_append() {
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk ${DEPLOY_DIR_IMAGE}/chroot-image-grouper.cpio.gz \
              --base 0x10000000 \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot

    cd ${DEPLOYDIR}
    rm -f ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
