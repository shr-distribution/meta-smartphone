require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "hammerhead"

DESCRIPTION = "Linux kernel for the LG Hammerhead (Nexus 5) device based on the offical \
source from Google/LG"

#ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=hammerhead user_debug=31 maxcpus=2 msm_watchdog_v2.enable=1 console=ttyHSL0,115200,n8"
ANDROID_BOOTIMG_CMDLINE = "gpt=enable lge.kcal=0|0|0|x lge.rev=rev_11 androidboot.laf androidboot.emmc=true fastboot=true androidboot.serialno=02c790c20937bd48 androidboot.bootloader=HHZ20h androidboot.baseband=msm androidboot.hardware.sku=D821 console=ttyHSL0,115200,n8"

ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02900000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x02700000"

inherit kernel_android

SRC_URI = " \
  git://github.com/Halium/android_kernel_lge_hammerhead.git;branch=halium-5.1 \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/cyanogenmod_hammerhead_defconfig ${WORKDIR}/defconfig
}

SRCREV = "17a8d0630b424d66981900e701ae2a452d4b8726"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
