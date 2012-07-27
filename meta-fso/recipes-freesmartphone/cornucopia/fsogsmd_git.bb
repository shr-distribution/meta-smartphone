require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

# use libsamsung-ipc-0.1 instead of AUTOREV or disable modem-samsung as temporary fix
# EXTRA_OECONF_append = " --disable-modem-samsung "

PV = "0.12.99+gitr${SRCPV}"
PR = "${INC_PR}.0"
