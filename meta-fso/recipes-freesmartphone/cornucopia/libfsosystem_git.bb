require cornucopia-from-git.inc
require cornucopia-base.inc

DESCRIPTION = "freesmartphone.org system library"
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.11.0+gitr${SRCPV}"
PE = "1"
PR = "${INC_PR}.0"
EXTRA_OECONF += "--enable-vala"
