require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^yggdrasil$"

DESCRIPTION = "Linux kernel for the Volla Phone device based on the offical \
source from Volla"

ANDROID_BOOTIMG_CMDLINE = "bootopt=64S3,32N2,64N2"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x14f88000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00e88000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x13f88000"

inherit kernel_android

SRC_URI = " \
    git://github.com/HelloVolla/android_kernel_volla_mt6763.git;branch=halium-9.0;name=kernel \
    file://k63v2_64_bsp_defconfig \
"
SRCREV_kernel = "131cee1a64a7cdaab2d13980ace175231d1b7d1c"

S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${WORKDIR}/k63v2_64_bsp_defconfig ${WORKDIR}/defconfig
}


KV = "4.4.146"
PV = "${KV}+gitr9cfaad"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
