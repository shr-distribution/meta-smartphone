DESCRIPTION = "SHR Phone UI Daemon"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SECTION = "x11/applications"
DEPENDS += " libfso-glib libshr-glib libphone-ui sqlite3 shr-specs"
SRCREV = "2c6eb06f45d20081f6b5a2146ac527813476b22c"
PV = "0.0.0+gitr${SRCPV}"

RREPLACES_${PN} = "shr-today"
RCONFLICTS_${PN} = "shr-today"

SRC_URI = "git://git.shr-project.org/repo/phoneuid.git;protocol=http;branch=master"
S = "${WORKDIR}/git"

inherit autotools-brokensep pkgconfig

EXTRA_OECONF = "\
       SPECS_PATH=${STAGING_DATADIR}/shr-specs \
"

FILES_${PN} += "${datadir}"

CONFFILES_${PN} = "${sysconfdir}/phoneuid.conf"

