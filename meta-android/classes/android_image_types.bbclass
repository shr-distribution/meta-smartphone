IMAGE_TYPES += "ext4.fastboot"
IMAGE_DEPENDS_ext4.fastboot = "android-tools-native"

ANDROID_BOARD_SYSTEMIMAGE_PARTITION_SIZE ?= "Please set to a valid value"
ANDROID_BOARD_FLASH_BLOCK_SIZE ?= "Please set to a valid value"

IMAGE_CMD:ext4.fastboot () {
    make_ext4fs -s -l ${ANDROID_BOARD_SYSTEMIMAGE_PARTITION_SIZE} \
                -b ${ANDROID_BOARD_FLASH_BLOCK_SIZE} \
                ${EXTRA_IMAGECMD} \
                ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.ext4.fastboot \
                ${IMAGE_ROOTFS}
}
