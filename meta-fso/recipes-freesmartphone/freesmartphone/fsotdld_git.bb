require cornucopia.inc
inherit fso-plugin
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PR = "${INC_PR}.6"
PV = "0.3.1+gitr${SRCPV}"
PE = "1"

DEPENDS += "libfsotransport libfsoresource json-glib libsoup-2.4 gpsd"
EXTRA_OECONF  = "--enable-provider-libgps"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 27"

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
	install -d ${D}${base_libdir}/systemd/system/
	install -m 0644 ${WORKDIR}/git/${PN}/data/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
}

