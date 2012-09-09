require recipes-kernel/linux/linux.inc

PE = "2"
KERNEL_RELEASE = "2.6.37"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRCREV = "e465ff7527fb085b4854b0e68aa6bc592229bf43"

SRC_URI = "\
  git://github.com/shr-distribution/linux.git;protocol=git;branch=nokia900/2.6.37/57.1 \
  file://defconfig \
"
S = "${WORKDIR}/git"

CMDLINE_nokia900 = "root=/dev/mmcblk0p1 rootwait rw console=ttyO2,115200n8 console=tty0 omapfb.vram=0:2M,1:2M,2:2M mtdoops.mtddev=2"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "nokia900"
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_nokia900 = "1"
