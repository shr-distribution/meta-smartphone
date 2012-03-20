DESCRIPTION = "A bluetooth/bluez manager written in elementary for embedded systems. Second version written now in Vala"
AUTHOR = "Pau Espin Pedrol (Sharwin_F) <pespin.shar@gmail.com>"
HOMEPAGE = "http://code.google.com/p/emtooth/"
SECTION = "x11/applications"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
DEPENDS = "glib-2.0 libeflvala"
RDEPENDS_${PN} += "bluez4 obexd"
RRECOMMENS_${PN} += "libasound-module-bluez kernel-module-btusb"

SRCREV = "169"
PV = "0.1+svnr${SRCPV}"
PR = "r2"

SRC_URI = "svn://emtooth.googlecode.com/svn/trunk;module=app2;proto=http \
"

EXTRA_OECONF += "--enable-fso"

S = "${WORKDIR}/app2"

inherit autotools vala

