require linux.inc

SECTION = "kernel"
DEPENDS += "android-image-utils-native chroot-image"
DESCRIPTION = "Linux kernel for the Samsung Crespo device based on the offical \
source from Samsung"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-crespo/master-next \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "35f4ab25c5e2a4ad0c0cda2d4a87131930e4b8d8"

KV = "3.0.8"
PR = "r2"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"

do_deploy_append() {
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk ${DEPLOY_DIR_IMAGE}/chroot-image-crespo.cpio.gz \
              --base 0x30000000 \
              --pagesize 4096 \
              --output ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGE_BASE_NAME}.fastboot

    cd ${DEPLOYDIR}
    rm -f ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
