require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRC_URI = "git://github.com/freesmartphone/mdbus.git;protocol=https"

SRCREV = "28202692d0b441000f4ddb8f347f72d1355021aa"

PV = "2.3.2+gitr${SRCPV}"

S = "${WORKDIR}/git"
