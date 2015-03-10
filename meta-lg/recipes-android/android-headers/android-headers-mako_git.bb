require recipes-android/android-headers/android-headers.inc

PV = "4.4.3+gitr${SRCPV}"
SRCREV = "890bef235d53ab09e140f02dcc27d1993df6e9df"

SRC_URI = "git://github.com/webOS-ports/phablet-headers.git;branch=cm-11.0;protocol=git"
S = "${WORKDIR}/git"
