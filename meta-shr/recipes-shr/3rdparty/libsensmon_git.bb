DESCRIPTION = "Library for easy-accessing the sensors supported by sensmon"
AUTHOR = "Michele Brocco <ssj2micvm@gmail.com>"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "glib-2.0"
RDEPENDS_${PN} = "glib-2.0"
RSUGGESTS_${PN} = ""
PV = "0.1+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://gitorious.org/sensor-monitor/libsensmon.git;protocol=git;branch=master"

SRCREV = "1e986a6d84d4a63ffa85a049bdfced00bad8d97a"
S = "${WORKDIR}/git"

inherit autotools-brokensep vala pkgconfig

FILES_${PN} += "${datadir}/${PN} ${datadir}/applications ${datadir}/pixmaps"

MAINTAINER = "Michele Brocco <ssj2micvm@gmail.com>"

PNBLACKLIST[libsensmon] ?= "BROKEN: not compatible with newer vala, http://errors.yoctoproject.org/Errors/Details/143058/"
