require recipes-kernel/linux/linux.inc

KERNEL_RELEASE = "2.6.37"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRCREV = "78ffc81db8911acdabd026ad3fdefa20b4e34e52"

SRC_URI = "\
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=nokia900/kernel-adaptation-n900-2.6.37-57.1 \
  file://defconfig \
"
S = "${WORKDIR}/git"

CMDLINE_nokia900 = "root=/dev/mmcblk0p1 rootwait rw console=ttyO2,115200n8 console=tty0 omapfb.vram=0:2M,1:2M,2:2M mtdoops.mtddev=2"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "nokia900"
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_nokia900 = "1"
