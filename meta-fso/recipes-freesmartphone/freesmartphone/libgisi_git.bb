require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "7f9352f67636562f182ca75ce00f6f3c18abfee2"
PV = "0.1.99+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/libgisi.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
