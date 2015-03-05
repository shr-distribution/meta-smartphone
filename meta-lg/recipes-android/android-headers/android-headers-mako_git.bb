require recipes-android/android-headers/android-headers.inc

PV = "4.2.2+gitr${SRCPV}"
SRCREV = "59648ed5520a2274ce2549b249416a0dad50583c"

SRC_URI = "git://github.com/webOS-ports/phablet-headers.git;branch=master;protocol=git"
S = "${WORKDIR}/git"
