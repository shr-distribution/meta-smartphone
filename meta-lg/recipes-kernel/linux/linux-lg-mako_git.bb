require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mako"

DESCRIPTION = "Linux kernel for the LG Mako (Nexus 4) device based on the offical \
source from Google/LG"

ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=mako lpj=67677"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80208000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81800000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x81100000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80200100"

inherit kernel_android

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=mako/3.4.0/ubp-5.1 \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/mako_defconfig ${WORKDIR}/defconfig
}

SRCREV = "1a90dd89eb24596d31919e2d25be80a059c5fd73"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
