#
# This class is used to create Android device compatible kernel images
#

KERNEL_RAM_BASE ?= "Please set to right value!"
RAMDISK_RAM_BASE ?= "0x00000000"
SECOND_RAM_BASE ?= "0x00000000"
TAGS_RAM_BASE ?= "0x00000000"
EXTRA_ABOOTIMG_ARGS ?= ""

do_compile[depends] += "initramfs-android-image:do_rootfs"
DEPENDS += "abootimg-native initramfs-android-image"

do_compile_append() {
    abootimg --create ${S}/boot.img \
             -k ${S}/${KERNEL_OUTPUT} \
             -r ${DEPLOY_DIR_IMAGE}/initramfs-android-image-${MACHINE}.cpio.gz \
             -c "cmdline=${CMDLINE}" \
             -c "kerneladdr=${KERNEL_RAM_BASE}" \
             -c "ramdiskaddr=${RAMDISK_RAM_BASE}" \
             -c "secondaddr=${SECOND_RAM_BASE}" \
             -c "tagsaddr=${TAGS_RAM_BASE}" \
             ${EXTRA_ABOOTIMG_ARGS}
}

do_install_append() {
    install -d ${D}/${KERNEL_IMAGEDEST}
    install -m 0644 ${S}/boot.img ${D}/${KERNEL_IMAGEDEST}
}

do_deploy_append() {
    cp ${S}/boot.img ${DEPLOYDIR}/${KERNEL_IMAGE_BASE_NAME}.fastboot
    ln -sf ${KERNEL_IMAGE_BASE_NAME}.fastboot ${DEPLOYDIR}/${KERNEL_IMAGE_SYMLINK_NAME}.fastboot
}

RDEPENDS_kernel-image += "abootimg"

pkg_postinst_kernel-image_append () {
    if [ x"$D" = "x" ] ; then
        if [ ! -e /boot/boot.img ] ; then
            # if the boot image is not available here something went wrong and we don't
            # continue with anything that can be dangerous
            exit 1
        fi

        BOOT_PARTITION_NAMES="LNX boot KERNEL"
        for i in $BOOT_PARTITION_NAMES; do
            path=$(find /dev -name "*$i*"|grep disk| head)
            [ -n "$path" ] && break
        done

        if [ -z "$path" ] ; then
            echo "Boot partition does not exist!"
            exit 1
        fi

        echo "Flashing the new kernel /boot/boot.img to $path"
        dd if=/boot/boot.img of=$path
    else
        exit 1
    fi
}

FILES_kernel-image += "/${KERNEL_IMAGEDEST}/boot.img"
