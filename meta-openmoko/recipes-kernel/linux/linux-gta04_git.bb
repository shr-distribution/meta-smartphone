require linux.inc

KERNEL_RELEASE = "3.2"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
PR = "r1"

SRCREV = "7c3549ed478ad6014c381062e9dc26967ae3c006"

SRC_URI = "\
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=gta04/neil \
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
CMDLINE_om-gta04 = "earlyprintk=ttyO2,115200n8 console=ttyO2,115200n8 mpurate=800 vram=12M omapfb.mode=dvi:1024x768MR-16@60 omapdss.def_disp=lcd root=/dev/mmcblk0p2 ro"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "om-gta04"

DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_om-gta04 = "1"
