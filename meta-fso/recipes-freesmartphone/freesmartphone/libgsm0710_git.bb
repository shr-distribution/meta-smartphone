require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "37ae2a2d27487ae20f0742d9044242d161df3741"
PV = "1.2.2+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://github.com/freesmartphone/libgsm0710;protocol=https;branch=master"
S = "${WORKDIR}/git"
