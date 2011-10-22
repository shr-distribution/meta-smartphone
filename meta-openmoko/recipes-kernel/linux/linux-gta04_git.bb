require linux.inc

KERNEL_RELEASE = "2.6.32"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
PR = "r3"

# hw-validation branch
SRCREV = "ed84b246532e43f52a39b1441f82a7c186c45119"
# master branch
#SRCREV = "4e8b9ef808fcb48dd0121b844f263a0f351917b4"

SRC_URI = "\
  git://git.goldelico.com/gta04-kernel.git;protocol=git;branch=hw-validation \
  file://sysfs_v7-build.patch \
  file://defconfig \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
#otherwise it gets extra '+' "2.6.37+"
#because:
#$ scripts/setlocalversion . => +
#$ make kernelversion => 2.6.37
#$ make kernelrelease => 2.6.37+
  rm -rf ${S}/.git
}
CMDLINE_om-gta04 = "root=/dev/mmcblk0p2 rootwait rw console=ttyS2,115200n8 console=tty0 omapfb.vram=0:2M,1:2M,2:2M mtdoops.mtddev=2"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "om-gta04"

DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_om-gta04 = "1"
