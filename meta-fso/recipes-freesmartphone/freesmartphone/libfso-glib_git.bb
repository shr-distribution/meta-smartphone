require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "e4f4b603b26e3601c316359684fe4a6bbca14462"
PV = "2012.04.20.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/${BPN}.git;protocol=git;branch=gdbus"
S = "${WORKDIR}/git"
