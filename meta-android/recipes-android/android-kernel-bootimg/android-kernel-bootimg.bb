#
# This recipe is used to create Android device compatible kernel images
#
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0;md5=801f80980d171dd6425610833a22dbe6"

PACKAGE_ARCH = "${MACHINE_ARCH}"

ANDROID_BOOTIMG_CMDLINE ?= ""
ANDROID_BOOTIMG_KERNEL_RAM_BASE ?= "Please set to right value!"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE ?= "0x00000000"
ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS ?= ""
KERNEL_IMAGEDEST ?= "boot"
KERNEL_IMAGETYPES ?= "${KERNEL_IMAGETYPE}"

DEPENDS = "initramfs-android-image virtual/kernel"
DEPENDS += "abootimg-native"

RDEPENDS_${PN} += "abootimg"

inherit deploy kernel-artifact-names
do_compile[depends] += "initramfs-android-image:do_image_complete virtual/kernel:do_deploy"

do_compile() {
    abootimg --create ${B}/boot.img \
             -k ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE} \
             -r ${DEPLOY_DIR_IMAGE}/initramfs-android-image-${MACHINE}.cpio.gz \
             -c "cmdline=${ANDROID_BOOTIMG_CMDLINE}" \
             -c "kerneladdr=${ANDROID_BOOTIMG_KERNEL_RAM_BASE}" \
             -c "ramdiskaddr=${ANDROID_BOOTIMG_RAMDISK_RAM_BASE}" \
             -c "secondaddr=${ANDROID_BOOTIMG_SECOND_RAM_BASE}" \
             -c "tagsaddr=${ANDROID_BOOTIMG_TAGS_RAM_BASE}" \
             ${ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS}
}

do_install() {
    install -d ${D}/${KERNEL_IMAGEDEST}
    install -m 0644 ${B}/boot.img ${D}/${KERNEL_IMAGEDEST}
}

do_deploy() {
    # We're probably interested only in zImage KERNEL_IMAGETYPE, but keep
    # the for loop for consistency with other bbclasses
    for type in ${KERNEL_IMAGETYPES} ; do
        cp ${B}/boot.img ${DEPLOYDIR}/${type}-${KERNEL_IMAGE_NAME}.fastboot
        ln -snvf ${type}-${KERNEL_IMAGE_NAME}.fastboot ${DEPLOYDIR}/${type}-${KERNEL_IMAGE_LINK_NAME}.fastboot
    done
}
addtask deploy after do_install before do_build

pkg_postinst_ontarget_${KERNEL_PACKAGE_NAME}-image_append () {
    if [ ! -e /boot/boot.img ] ; then
        # if the boot image is not available here something went wrong and we don't
        # continue with anything that can be dangerous
        exit 1
    fi

    BOOT_PARTITION_NAMES="LNX boot boot_a KERNEL"
    for i in $BOOT_PARTITION_NAMES; do
        path=$(find /dev -name "$i"|grep disk| head -n 1)
        [ -n "$path" ] && break
    done

    if [ -z "$path" ] ; then
        echo "Boot partition does not exist!"
        exit 1
    fi

    echo "Flashing the new kernel /boot/boot.img to $path"
    dd if=/boot/boot.img of=$path
}

FILES_${PN} += "/${KERNEL_IMAGEDEST}/boot.img"
