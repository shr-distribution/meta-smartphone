DESCRIPTION = "SHR Phone FSO Daemon"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SECTION = "x11/applications"
DEPENDS += " libfso-glib libshr-glib sqlite3 shr-specs libfsoframework"
SRCREV = "cc19fc95ae1a36f1a3d12bd13fce751cf4cf095f"
PV = "0.0.0+gitr${SRCPV}"
PR = "r5"

SRC_URI = "git://git.shr-project.org/repo/phonefsod.git;protocol=http;branch=master \
           file://0001-phonefsod-dbus-changes-for-new-gdbus.patch \
	   file://0002-configure-drop-check-for-gdbus-codegen-as-new-python.patch"
S = "${WORKDIR}/git"

inherit autotools update-rc.d

INITSCRIPT_NAME = "phonefsod"
INITSCRIPT_PARAMS = "defaults 75"

CONFFILES_${PN} = "${sysconfdir}/phonefsod.conf"


