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
  git://github.com/ubports/android_kernel_google_msm.git;branch=ubp-5.1 \
  file://0001-Force-WLAN-to-be-build-as-module.patch \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/mako_defconfig ${WORKDIR}/defconfig
}

SRCREV = "c2beb2ae393193c10cdd4446f98127dbf4b06768"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
