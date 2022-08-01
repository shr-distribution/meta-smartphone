require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"

DESCRIPTION = "Linux kernel for HP Touchpad"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT console=ttyMSM0,115200,n8"
#ANDROID_BOOTIMG_CMDLINE = "msm.vram=200m cma=300m g_mass_storage.removable=y LUNEOS_NO_OUTPUT_REDIRECT g_ffs.idVendor=0x18d1 g_ffs.idProduct=0xd001"
ANDROID_BOOTIMG_CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT console=ttyMSM0,115200,n8"

inherit kernel

LINUX_VERSION ?= "5.19-rc7"
LINUX_VERSION_EXTENSION = "-luneos"
#LINUX_KMETA_BRANCH = "yocto-${LINUX_VERSION}"
LINUX_KMETA_BRANCH = "master"
KMETA = "kernel-meta"

SRCREV_machine = "d7b1b2798140393df7745849a871ce8cd880d712"
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

do_deploy[depends] += "initramfs-uboot-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

INITRAMFS_UIMAGE = "initramfs-uboot-image-${MACHINE}.rootfs.cpio.gz.u-boot"

do_deploy:append() {
    if [ ! -e ${DEPLOY_DIR_IMAGE}/${INITRAMFS_UIMAGE} ] ; then
        bbfatal "Required initramfs uImage ${DEPLOY_DIR_IMAGE}/${INITRAMFS_UIMAGE} is not available!"
    fi

    # pack kernel+dtb as uboot image
    echo "pack kernel+dtb as uboot image..."
    cat ${DEPLOYDIR}/${KERNEL_IMAGETYPE} ${B}/${KERNEL_OUTPUT_DIR}/dts/${KERNEL_DEVICETREE} > ${B}/${KERNEL_OUTPUT}-dtb
    
	ENTRYPOINT=${UBOOT_ENTRYPOINT}
	if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
		ENTRYPOINT=`${HOST_PREFIX}nm ${B}/vmlinux | \
			awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
	fi

    echo "ENTRYPOINT = $ENTRYPOINT"
    uboot-mkimage -A arm -O Linux -T kernel -C none -a ${UBOOT_LOADADDRESS} -e ${ENTRYPOINT} \
                  -n "${DISTRO_NAME}/${PV}/${MACHINE}" \
                  -d ${B}/${KERNEL_OUTPUT}-dtb ${B}/arch/${ARCH}/boot/uImage

    # now pack kernel and initramfs together
    echo "now pack kernel and initramfs together..."
    uboot-mkimage  -A arm -O Linux -T multi -n 'HP Touchpad boot' -C none \
        -e 0 -a 0 -d ${B}/arch/${ARCH}/boot/uImage:${DEPLOY_DIR_IMAGE}/${INITRAMFS_UIMAGE} \
        ${DEPLOYDIR}/uImage-dtb-${KERNEL_IMAGETYPE}
}
