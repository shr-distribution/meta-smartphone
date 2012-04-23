require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "38d23dd55652f68343ae553672b6bcceacacee9e"
PV = "0.2.0+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/libgisi.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
