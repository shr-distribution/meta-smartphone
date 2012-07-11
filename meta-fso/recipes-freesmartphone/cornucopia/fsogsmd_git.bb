require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

# Should be moved to fsogsmd.inc when switching to 0.12
DEPENDS += "libfsotest"

PV = "0.11.99+gitr${SRCPV}"
PR = "${INC_PR}.2"
