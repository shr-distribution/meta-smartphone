require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "490c63bf3d7c631464712f27f4698643d148c0d7"
PV = "2012.04.20.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${SRCNAME}.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
