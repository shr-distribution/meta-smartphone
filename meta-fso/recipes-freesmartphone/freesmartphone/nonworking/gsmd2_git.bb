DESCRIPTION = "GSM 07.07 phone server"
HOMEPAGE = "http://www.freesmartphone.org/mediawiki/index.php/Implementations/gsmd2"
AUTHOR = "Ixonos Team"
SECTION = "console/network"
DEPENDS = "dbus dbus-glib"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SRCREV = "c16883a079aeff8780e5d461ec4e8348537ab4d8"
PV = "0.1.0+gitr${SRCPV}"
PE = "1"
PR = "r2"

SRC_URI = "git://github.com/freesmartphone/gsmd2;protocol=https;branch=master"
S = "${WORKDIR}/git"

inherit autotools

EXTRA_OECONF = "--disable-tests"

RDEPENDS_${PN} = "fso-gsm0710muxd"
