DESCRIPTION = "SHR Phone FSO Daemon"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SECTION = "x11/applications"
DEPENDS += " libfso-glib libshr-glib sqlite3 shr-specs libfsoframework"
SRCREV = "58cd417800f810c439be54b72dfb585b172921ac"
PV = "0.0.0+gitr${SRCPV}"
PR = "r6"

SRC_URI = "git://git.shr-project.org/repo/phonefsod.git;protocol=http;branch=master \
  file://${PN}.service \
"
S = "${WORKDIR}/git"

inherit autotools update-rc.d systemd

SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

PACKAGES =+ "${PN}-systemd"
FILES_${PN}-systemd += "${base_libdir}/systemd"
RDEPENDS_${PN}-systemd += "${PN}"

INITSCRIPT_NAME = "phonefsod"
INITSCRIPT_PARAMS = "defaults 75"

CONFFILES_${PN} = "${sysconfdir}/phonefsod.conf"

do_install_append() {
        install -d ${D}${base_libdir}/systemd/system/
        install -m 0644 ${WORKDIR}/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
}
