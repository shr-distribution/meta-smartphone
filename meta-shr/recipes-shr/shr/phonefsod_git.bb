DESCRIPTION = "SHR Phone FSO Daemon"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SECTION = "x11/applications"
DEPENDS += " libfso-glib libshr-glib sqlite3 shr-specs libfsoframework"
SRCREV = "42dd49e95660ca1237b244bb45fe6474d9241bd1"
PV = "0.0.0+gitr${SRCPV}"
PR = "r9"

SRC_URI = "git://git.shr-project.org/repo/phonefsod.git;protocol=http;branch=master \
  file://${PN}.service \
"
S = "${WORKDIR}/git"

inherit autotools update-rc.d systemd

SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

INITSCRIPT_NAME = "phonefsod"
INITSCRIPT_PARAMS = "defaults 75"

CONFFILES_${PN} = "${sysconfdir}/phonefsod.conf"
