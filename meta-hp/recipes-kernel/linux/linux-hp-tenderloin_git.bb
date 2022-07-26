require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"

DESCRIPTION = "Linux kernel for HP Touchpad"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT console=ttyMSM0,115200,n8"
#ANDROID_BOOTIMG_CMDLINE = "msm.vram=200m cma=300m g_mass_storage.removable=y LUNEOS_NO_OUTPUT_REDIRECT g_ffs.idVendor=0x18d1 g_ffs.idProduct=0xd001"
ANDROID_BOOTIMG_CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT console=ttyMSM0,115200,n8"

inherit kernel_android

LINUX_VERSION ?= "5.19-rc7"
LINUX_VERSION_EXTENSION = "-luneos"
#LINUX_KMETA_BRANCH = "yocto-${LINUX_VERSION}"
LINUX_KMETA_BRANCH = "master"
KMETA = "kernel-meta"

SRCREV_machine = "6d76455e1dba2ef3be566326ce821abec0c709b2"
SRCREV_meta = "f55df88ad1b189c955984ead7f91389e2676e413"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=tenderloin/5.19/mainline;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
"

S = "${WORKDIR}/git"

KV = "5.19-rc7"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_deploy[depends] += "initramfs-android-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

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
