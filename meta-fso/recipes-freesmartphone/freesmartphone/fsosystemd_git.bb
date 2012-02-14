require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.0.1+gitr${SRCPV}"
PR = "${INC_PR}.2"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "start 39 S ."

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

PACKAGES =+ "${PN}-systemd"
FILES_${PN}-systemd += "${base_libdir}/systemd"
RDEPENDS_${PN}-systemd += "${PN}"

SRC_URI += "file://${PN}"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
	if [ -e ${WORKDIR}/git/${PN}/data/${PN}.service ] ; then
		install -d ${D}${base_libdir}/systemd/system/
		install -m 0644 ${WORKDIR}/git/${PN}/data/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
	fi
}
