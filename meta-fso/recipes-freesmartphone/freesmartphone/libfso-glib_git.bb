require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "bb0ef5fd1fd12e4a1f4f40deeee3d5f01706d479"
PV = "2012.07.27.1+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://github.com/freesmartphone/${BPN};protocol=https;branch=master"
S = "${WORKDIR}/git"
