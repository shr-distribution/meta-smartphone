require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "574526b825cca97be072b6369cabb56bf551186d"
PV = "2012.05.24.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${SRCNAME}.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
