DESCRIPTION = "High Level DBus Introspection Daemon"
HOMEPAGE = "http://www.freesmartphone.org/mediawiki/index.php/Implementations/DbusHlid"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
SECTION = "console/network"
DEPENDS = "vala-native dbus dbus-glib"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SRCREV = "154652fdbd2cf68e03a3c91c49ff2ece8ffd7288"
PV = "1.0.1+gitr${SRCPV}"
PE = "1"
PR = "r1"

SRC_URI = "git://github.com/freesmartphone/dbus-hlid;protocol=https;branch=master"
S = "${WORKDIR}/git"

inherit autotools

FILES_${PN} += "${datadir} ${sysconfdir}"
