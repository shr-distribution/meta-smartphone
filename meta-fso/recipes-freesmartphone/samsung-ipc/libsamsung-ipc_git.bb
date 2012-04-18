DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "openssl mtd-utils"
SRC_URI = "git://github.com/morphis/libsamsung-ipc.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "ed22d1fdb6205a73369e3ee311d8c605ded3e70e"
PV = "0.1.0+gitr${SRCPV}"
PR = "r2"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl ${bindir}/samsung_ipc_forward"
FILES_${PN}-dev += "${datadir}/vala/vapi/samsung-ipc-1.0.vapi"

LEAD_SONAME = "libsamsung-ipc.so"
