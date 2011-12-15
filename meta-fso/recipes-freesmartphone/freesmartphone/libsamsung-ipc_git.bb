DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"
DEPENDS = "openssl"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "cb37d8dc7b975995a3d56e7d157b391699650f7b"
PV = "0.1.0+gitr${SRCPV}"
PR = "r1"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl"
FILES_${PN}-dev += "${datadir}/vala/vapi/samsung-ipc-1.0.vapi"

LEAD_SONAME = "libsamsung-ipc.so"
