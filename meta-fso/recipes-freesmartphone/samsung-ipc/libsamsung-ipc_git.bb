require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "ed22d1fdb6205a73369e3ee311d8c605ded3e70e"
PV = "0.1.0+gitr${SRCPV}"

SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
