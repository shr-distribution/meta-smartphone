DESCRIPTION = "Forward a serial using TCP/IP"
AUTHOR = "Holger 'Zecke' Freyther'"
LICENSE = "GPL"
SECTION = "console/devel"
SRCREV = "00dbec2636ae0385ad028587e20e446272ff97ec"
PV = "1.1+gitr${SRCPV}"
PE = "1"
PR = "r0"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=ebb5c50ab7cab4baeffba14977030c07"

SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia.git/;protocol=git"
S = "${WORKDIR}/git/tools/serial_forward"

inherit autotools
