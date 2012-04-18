require cornucopia-from-git.inc
require cornucopia-base.inc

inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PR = "${INC_PR}.0"
PV = "0.11.0+gitr${SRCPV}"
PE = "1"

DEPENDS += "libfsotransport libfsoresource json-glib libsoup-2.4 gpsd"
EXTRA_OECONF  = "--enable-provider-libgps"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 27"

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

SRC_URI += "file://${PN}"

do_install_append() {
	install -d ${D}${sysconfdir}/init.d/
	install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
	install -d ${D}${systemd_unitdir}/system/
	install -m 0644 ${S}/data/${PN}.service ${D}${systemd_unitdir}/system/${PN}.service
}

