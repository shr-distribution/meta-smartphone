require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.11.0+gitr${SRCPV}"
PR = "${INC_PR}.0"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "start 39 S ."

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

SRC_URI += "file://${PN}"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
	if [ -e ${S}/data/${PN}.service ] ; then
		install -d ${D}${systemd_unitdir}/system/
		install -m 0644 ${S}/data/${PN}.service ${D}${systemd_unitdir}/system/${PN}.service
	fi
}
