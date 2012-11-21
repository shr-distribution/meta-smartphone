inherit image_types kernel-arch

oe_fastboot () {
    mkbootimg --kernel ${S}/${KERNEL_OUTPUT} \
              --ramdisk ${DEPLOY_DIR_IMAGE}/chroot-image-tuna.cpio.gz \
              --cmdline "${CMDLINE}" \
              --base 0x80000000 \
              --output ${DEPLOY_DIR_IMAGE}/$1.fastboot
}

COMPRESS_DEPENDS_fastboot = "android-image-utils-native"
COMPRESS_CMD_fastboot     = "oe_fastboot ${IMAGE_NAME}.rootfs.${type} none"

IMAGE_TYPES += "fastboot"
