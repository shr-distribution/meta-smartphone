require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "7d6f56c4f67416658c0b490d828ffe0938381f5f"
PV = "0.2.0+gitr${SRCPV}"

SAMSUNGIPC_BRANCH ?= "master"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=${SAMSUNGIPC_BRANCH}"
S = "${WORKDIR}/git"
