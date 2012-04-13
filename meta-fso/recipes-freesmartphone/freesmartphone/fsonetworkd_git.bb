require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.11.0+gitr${SRCPV}"
PE = "1"
PR = "${INC_PR}.0"
RDEPENDS_${PN} += "iptables"
