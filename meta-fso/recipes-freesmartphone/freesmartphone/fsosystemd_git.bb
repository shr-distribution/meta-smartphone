require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.0.1+gitr${SRCPV}"
PR = "${INC_PR}.0"

inherit update-rc.d

INITSCRIPT_NAME = "fsosystemd"
INITSCRIPT_PARAMS = "start 39 S ."

SRC_URI += "file://fsosystemd"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/fsosystemd ${D}${sysconfdir}/init.d/
}

