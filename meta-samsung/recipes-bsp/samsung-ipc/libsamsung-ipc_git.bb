DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"
DEPENDS = "openssl"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "3d4089b973c72520ceab6d7b576a499cb89547fd"
PV = "0.1.0+gitr${SRCPV}"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl"

LEAD_SONAME = "libsamsung-ipc.so"
