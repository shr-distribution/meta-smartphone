require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "7867e63de20cf6fb8d62fae8ab56eb50aa2bebf8"
PV = "0.10.99+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/framework.git;protocol=git;branch=master \
"

S = "${WORKDIR}/git"
