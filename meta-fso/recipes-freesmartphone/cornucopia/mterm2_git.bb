require cornucopia-base.inc
require cornucopia-from-git.inc

DESCRIPTION = "mterm is a versatile muxer-aware terminal program"
HOMEPAGE = "http://www.freesmartphone.org/index.php/Implementations/fso-term"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
SECTION = "console/network"
DEPENDS = "dbus dbus-glib readline libfsoframework libfsotransport"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
# we cannot use FSO_CORNUCOPIA_SRCREV without fso-autorev, because it needs newer
# libfso-glib and fso-specs
#SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
SRCREV = "30bbda292e432597a127a090ff5f7c06e7c9675c"
PV = "1.9.0+gitr${SRCPV}"
PR = "${INC_PR}.1"

S = "${WORKDIR}/git/tools/${PN}"
SRC_URI += "file://unbump.libfso-glib.version.patch"

FILES_${PN} += "${datadir}"
