DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
DEPENDS = "openssl mtd-utils"
PE = "1"

SRC_URI = "http://downloads.freesmartphone.org/sources/${BPN}/${P}.tar.bz2"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl ${bindir}/samsung_ipc_forward"
FILES_${PN}-dev += "${datadir}/vala/vapi/samsung-ipc-1.0.vapi"

LEAD_SONAME = "libsamsung-ipc.so"
