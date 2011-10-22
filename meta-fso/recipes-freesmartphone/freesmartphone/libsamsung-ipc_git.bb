DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"
DEPENDS = "openssl"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "eb4b7e1bba7db0368042a44d7f1e44c70a3a5b70"
PV = "0.1.0+gitr${SRCPV}"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl"

LEAD_SONAME = "libsamsung-ipc.so"
