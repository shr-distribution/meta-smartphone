require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "806dfa3e13c9d78e40b1db332e11f2515b9e740e"
PV = "0.10.99+gitr${SRCPV}"
PR = "r0"

SRC_URI = "git://github.com/freesmartphone/framework;protocol=https;branch=master"

S = "${WORKDIR}/git"
