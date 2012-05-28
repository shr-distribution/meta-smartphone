require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "6fe4f43495e23c004cc55e22215d4d2c59ad4baf"
PV = "2012.05.24.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${SRCNAME}.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
