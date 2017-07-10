require recipes-kernel/linux/linux.inc

DESCRIPTION = "Linux kernel for HP Touchpad"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin"

SRC_URI = " \
  git://github.com/Tofee/android_kernel_htc_msm8960.git;protocol=git;branch=tenderloin/3.4/cm-12.1 \
  file://defconfig \
"
S = "${WORKDIR}/git"

SRCREV = "78c953925961fea4e31e578ce5b5e321d422874a"

do_compile[depends] += "initramfs-android-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native initramfs-android-image"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

KV = "3.4.106"
PV = "${KV}+gitr${SRCPV}"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_compile_prepend() {
    if [ -e ${B}/${KERNEL_OUTPUT} ] ; then
        rm ${B}/${KERNEL_OUTPUT}
    fi
}

do_compile_append() {
    if [ -e ${DEPLOY_DIR_IMAGE}/initramfs-android-image-${MACHINE}.cpio.gz ] ; then
        cp ${B}/${KERNEL_OUTPUT} ${B}/${KERNEL_OUTPUT}.orig
    
        # pack initramfs as uboot image
        echo "pack initramfs as uboot image..."
        uboot-mkimage -A arm -O Linux -T ramdisk -n 'HP Touchpad boot initrd' -C none \
            -e 0 -a 0 -d ${DEPLOY_DIR_IMAGE}/initramfs-android-image-${MACHINE}.cpio.gz \
            ${B}/initramfs-android-image-${MACHINE}.uImage

        # now pack kernel and initramfs together
        echo "now pack kernel and initramfs together..."
        uboot-mkimage  -A arm -O Linux -T multi -n 'HP Touchpad boot' -C none \
            -e 0 -a 0 -d ${B}/${KERNEL_OUTPUT}.orig:${B}/initramfs-android-image-${MACHINE}.uImage \
            ${B}/${KERNEL_OUTPUT}
    else
        bberror "Could not find ${INITRAMFS_IMAGE}-${MACHINE}.cpio.gz in ${DEPLOY_DIR_IMAGE}"
    fi
}
