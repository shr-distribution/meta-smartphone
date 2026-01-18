require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"

DESCRIPTION = "Linux kernel for HP Touchpad"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Note: cmdline is given via our DTS

inherit kernel

LINUX_VERSION_EXTENSION = "-luneos"
LINUX_KMETA_BRANCH = "yocto-6.18"
KMETA = "kernel-meta"
KBUILD_DEFCONFIG:tenderloin = "tenderloin_debug_defconfig"
KCONFIG_MODE = "alldefconfig"

SRCREV_machine = "77b0c4fe6ce0774b986cb5c70fb758f8f09a9570"
SRCREV_meta = "8ac9b1baf5d3cc1cb53a87a449b52f253dc32cab"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=tenderloin/6.18/upstream-patches;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
"

LINUX_VERSION = "6.18"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_deploy[depends] += "initramfs-uboot-image:do_image_complete"
DEPENDS += "u-boot-mkimage-native libyaml-native python3-yamllint-native coreutils-native python3-dtschema-native"
KERNEL_OUTPUT ?= "${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE}"

INITRAMFS_UIMAGE = "initramfs-uboot-image-${MACHINE}.cpio.gz.u-boot"

#EXTRA_OEMAKE += "CHECK_DTBS=y"

#do_compile:prepend() {
#    oe_runmake dtbs_check
#}

do_recompile_dtb() {
    cd ${B}
    oe_runmake ${KERNEL_DEVICETREE}
}
addtask recompile_dtb before do_compile after do_configure

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
