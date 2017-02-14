require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

PV = "0.12.99+gitr${SRCPV}"
PR = "${INC_PR}.0"

PNBLACKLIST[fsotdld] ?= "Depends on blacklisted libfso-glib"
