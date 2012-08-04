require recipes-kernel/linux/linux.inc

SECTION = "kernel"

do_compile[depends] += "chroot-image:do_deploy"
DEPENDS += "android-image-utils-native chroot-image"
DESCRIPTION = "Linux kernel for the Samsung Tuna device based on the offical \
source from Samsung"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-tuna/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "0dceae86aba8e3ae8ef0fec329ad5353fa7a6d82"

KV = "3.0.31"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"

do_deploy_append() {
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk ${DEPLOY_DIR_IMAGE}/chroot-image-tuna.cpio.gz \
              --base 0x30000000 \
              --pagesize 4096 \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot

    cd ${DEPLOYDIR}
    rm -f ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
