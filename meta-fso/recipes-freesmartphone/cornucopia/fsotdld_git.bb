require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

PV = "0.12.99+gitr${SRCPV}"
PR = "${INC_PR}.0"

PNBLACKLIST[fsotdld] ?= "Depends on blacklisted libfso-glib - the recipe will be removed on 2017-09-01 unless the issue is fixed"
