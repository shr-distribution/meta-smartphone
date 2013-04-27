#
# This class is used to create Android device compatible kernel images
#

KERNEL_RAM_BASE ?= "Please set to right value!"
EXTRA_MKBOOTIMG_ARGS ?= ""

do_deploy[depends] += "chroot-image:do_build"
DEPENDS += "android-image-utils-native chroot-image"

do_deploy_append() {
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk ${DEPLOY_DIR_IMAGE}/chroot-image-${MACHINE}.cpio.gz \
              --base ${KERNEL_RAM_BASE} \
              --output ${DEPLOYDIR}/${KERNEL_IMAGE_BASE_NAME}.fastboot \
              ${EXTRA_MKBOOTIMG_ARGS}

    cd ${DEPLOYDIR}
    rm -f ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
