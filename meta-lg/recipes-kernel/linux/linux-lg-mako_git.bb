require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mako"

DESCRIPTION = "Linux kernel for the LG Mako (Nexus 4) device based on the offical \
source from Google/LG"

CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=mako lpj=67677"
KERNEL_RAM_BASE = "0x80208000"
RAMDISK_RAM_BASE = "0x81800000"
SECOND_RAM_BASE = "0x81100000"
TAGS_RAM_BASE = "0x80200100"

inherit kernel_android

SRC_URI = " \
  git://github.com/Tofee/android_kernel_google_msm.git;protocol=git;branch=mako/3.4/cm-12.1 \
  file://defconfig \
"
S = "${WORKDIR}/git"

SRCREV = "7d37fad3f1db6f01fe322b2ef0523c9259d7d96e"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
