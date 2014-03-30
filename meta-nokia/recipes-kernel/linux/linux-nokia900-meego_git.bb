require recipes-kernel/linux/linux.inc

PE = "2"
KERNEL_RELEASE = "3.13"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRCREV = "c089e5f8ba2d44afd2db2c2042d43ca70205c536"

SRC_URI = "\
  git://github.com/shr-distribution/linux.git;protocol=git;branch=nokia900/3.13/master \
  file://defconfig \
"
S = "${WORKDIR}/git"

KERNEL_EXTRA_ARGS = "LOADADDR=${UBOOT_LOADADDRESS}"

CMDLINE_nokia900 = "root=/dev/mmcblk0p1 rootwait rw console=ttyO2,115200n8 console=tty0 omapfb.vram=0:2M,1:2M,2:2M mtdoops.mtddev=2"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "nokia900"
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_nokia900 = "1"
