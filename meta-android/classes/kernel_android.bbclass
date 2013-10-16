#
# This class is used to create Android device compatible kernel images
#

KERNEL_RAM_BASE ?= "Please set to right value!"
EXTRA_ABOOTIMG_ARGS ?= ""

do_deploy[depends] += "initramfs-android-image:do_build"
DEPENDS += "abootimg-native initramfs-android-image"

do_deploy_append() {
    abootimg --create ${DEPLOYDIR}/${KERNEL_IMAGE_BASE_NAME}.fastboot \
             -k ${S}/${KERNEL_OUTPUT} \
             -r ${DEPLOY_DIR_IMAGE}/initramfs-android-image-${MACHINE}.cpio.gz \
             -c "kerneladdr=${KERNEL_RAM_BASE}" \
             ${EXTRA_ABOOTIMG_ARGS}

    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${DEPLOYDIR}/${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}
