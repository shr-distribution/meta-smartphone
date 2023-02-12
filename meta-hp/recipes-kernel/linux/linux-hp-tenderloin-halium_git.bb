require recipes-kernel/linux/linux.inc

DESCRIPTION = "Linux kernel for HP Touchpad (Halium based)"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin-halium"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=tenderloin/3.4/halium-9.0;protocol=https"
S = "${WORKDIR}/git"

CMDLINE = "androidboot.selinux=permissive  androidboot.hardware=tenderloin"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm/configs/tenderloin_android_defconfig ${WORKDIR}/defconfig
}

do_deploy[depends] += "initramfs-android-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

SRCREV = "57a74cc9b4b63d3f6a198dcc3215a0cbf3c657e1"

KV = "3.4.113"
PV = "${KV}+gitr${SRCPV}"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

INITRAMFS_NAME = "initramfs-android-image-${MACHINE}.cpio.gz"
INITRAMFS_UIMAGE = "initramfs-android-image-${MACHINE}.uImage"

do_deploy:append() {
    if [ ! -e ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} ] ; then
        bbfatal "Required initramfs image ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} is not available!"
    fi

    cp ${DEPLOYDIR}/${KERNEL_IMAGETYPE} ${B}/${KERNEL_OUTPUT}.orig

    # pack initramfs as uboot image
    echo "pack initramfs as uboot image..."
    uboot-mkimage -A arm -O Linux -T ramdisk -n 'HP Touchpad boot initrd' -C none \
        -e 0 -a 0 -d ${DEPLOY_DIR_IMAGE}/${INITRAMFS_NAME} \
        ${B}/${INITRAMFS_UIMAGE}

    # now pack kernel and initramfs together
    echo "now pack kernel and initramfs together..."
    uboot-mkimage  -A arm -O Linux -T multi -n 'HP Touchpad boot' -C none \
        -e 0 -a 0 -d ${B}/${KERNEL_OUTPUT}.orig:${B}/${INITRAMFS_UIMAGE} \
        ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
}
