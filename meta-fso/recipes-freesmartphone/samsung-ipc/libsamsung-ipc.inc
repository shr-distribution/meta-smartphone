DESCRIPTION = "A library implemented the modem IPC protocol from Samsung"
SECTION = "libs/network"
AUTHOR = " Joerie de Gram, PaulK, Simon Busch"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "openssl mtd-utils"
PE = "1"

SHRT_VER = "${@bb.data.getVar('PV',d,1).split('.')[0]}.${@bb.data.getVar('PV',d,1).split('.')[1]}"
SRC_URI = "http://downloads.freesmartphone.org/sources/${BPN}/${SHRT_VER}/${P}.tar.bz2"

inherit autotools

PACKAGES =+ "${PN}-tools"
FILES_${PN}-tools = "${bindir}/modemctrl ${bindir}/samsung_ipc_forward"
FILES_${PN}-dev += "${datadir}/vala/vapi/samsung-ipc-1.0.vapi"

LEAD_SONAME = "libsamsung-ipc.so"
