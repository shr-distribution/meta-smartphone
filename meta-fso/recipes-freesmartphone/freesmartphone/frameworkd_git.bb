require ${BPN}.inc

DEFAULT_PREFERENCE = "-1"

SRCREV = "806dfa3e13c9d78e40b1db332e11f2515b9e740e"
PV = "0.10.99+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/framework.git;protocol=git;branch=master"

S = "${WORKDIR}/git"

PNBLACKLIST[frameworkd] ?= "Runtime depends on blacklisted fsousaged"

PNBLACKLIST[frameworkd] ?= "Runtime depends on blacklisted frameworkd-dev"
