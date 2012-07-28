require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "845bdc7cf07c391ca42a99e16f97d14c0cc1f486"
PV = "2012.07.27.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${SRCNAME}.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
