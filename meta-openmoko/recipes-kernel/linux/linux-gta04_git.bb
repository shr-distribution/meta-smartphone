require linux.inc

KERNEL_RELEASE = "3.1"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
PR = "r0"

SRCREV = "8d109e588b840c288ea1ad4d8f166080510be55a"

SRC_URI = "\
  git://git.goldelico.com/gta04-kernel.git;protocol=git;branch=master \
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
