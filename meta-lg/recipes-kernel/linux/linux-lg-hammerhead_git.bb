require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "hammerhead"

DESCRIPTION = "Linux kernel for the LG Hammerhead (Nexus 5) device based on the offical \
source from Google/LG"

CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=hammerhead user_debug=31 maxcpus=2 msm_watchdog_v2.enable=1"
KERNEL_RAM_BASE = "0x00008000"
RAMDISK_RAM_BASE = "0x02900000"
SECOND_RAM_BASE = "0x00f00000"
TAGS_RAM_BASE = "0x02700000"

inherit kernel_android

SRC_URI = " \
  git://github.com/Tofee/android_kernel_lge_hammerhead.git;protocol=git;branch=hammerhead/3.4/cm-12.1 \
  file://defconfig \
"
S = "${WORKDIR}/git"

SRCREV = "8fd41b63491f3512ce7ee536d080d90facb57f45"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
