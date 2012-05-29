require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

PV = "2.3.0+gitr${SRCPV}"
PR = "${INC_PR}.0"

S = "${WORKDIR}/git/tools/${PN}"
