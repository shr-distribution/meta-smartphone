DESCRIPTION = "GSM 07.10 muxer userspace daemon (FSO Branch)"
HOMEPAGE = "http://www.freesmartphone.org/mediawiki/index.php/Implementations/gsm0710muxd"
SECTION = "console/network"
DEPENDS = "dbus dbus-glib"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SRCREV = "1d69fb5b666ac2a9e54e46978c7afa8fe5dfc3c9"
PV = "0.9.3.1+gitr${SRCPV}"
PE = "1"
PR = "r1"

SRC_URI = "${FREESMARTPHONE_GIT}/gsm0710muxd.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

inherit autotools pkgconfig

RDEPENDS_${PN} = "dbus dbus-glib"
RCONFLICTS_${PN} = "gsm0710muxd"
RREPLACES_${PN} = "gsm0710muxd"

FILES_${PN} += "${datadir} ${sysconfdir}"

PNBLACKLIST[fso-gsm0710muxd] ?= "Fails to build with RSS http://errors.yoctoproject.org/Errors/Details/130517/"
