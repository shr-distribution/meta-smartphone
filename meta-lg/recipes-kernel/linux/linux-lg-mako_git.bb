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
  file://0001-compiler-gcc.h-Add-gcc-recommended-GCC_VERSION-macro.patch \
  file://0001-compiler-gcc-integrate-the-various-compiler-gcc-345-.patch \
  file://0001-Force-WLAN-to-be-build-as-module.patch \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/mako_defconfig ${WORKDIR}/defconfig
}

SRCREV = "5f967233e931cd1d5fb542b3221a37f5520c4f30"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
