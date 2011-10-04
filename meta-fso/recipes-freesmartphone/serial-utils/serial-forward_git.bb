DESCRIPTION = "Forward a serial using TCP/IP"
AUTHOR = "Holger 'Zecke' Freyther'"
LICENSE = "GPL"
SECTION = "console/devel"
SRCREV = "2725c6c18cace2acaf4f20c0748300dcea4dd90c"
PV = "1.1+gitr${SRCPV}"
PE = "1"
PR = "r0"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=ebb5c50ab7cab4baeffba14977030c07"

SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia.git/;protocol=git"
S = "${WORKDIR}/git/tools/serial_forward"

inherit autotools
