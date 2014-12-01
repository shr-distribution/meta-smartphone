require ${BPN}.inc
require cornucopia-from-git.inc

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"

SRC_URI += "file://0001-fsogsmd-remove-global-theServiceDependencies-variabl.patch"
PV = "0.12.99+gitr${SRCPV}"
PR = "${INC_PR}.0"
