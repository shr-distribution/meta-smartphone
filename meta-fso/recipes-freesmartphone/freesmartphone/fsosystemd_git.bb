require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.0.1+gitr${SRCPV}"
PR = "${INC_PR}.3"

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
		install -d ${D}${base_libdir}/systemd/system/
		install -m 0644 ${S}/data/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
	fi
}
