DESCRIPTION = "NumberX is a mathematical puzzle game that will challenge your mental math abilities! "
HOMEPAGE = "http://code.google.com/p/numberx/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "x11/applications"
DEPENDS = "python-native"

SRCREV = "4"
PV = "1.0.0+svnr${SRCPV}"
PR = "r3"

S = "${WORKDIR}/trunk"

SRC_URI = "svn://numberx.googlecode.com/svn;module=trunk;protocol=http"

inherit distutils

RDEPENDS_${PN} += "python-math python-elementary" 

FILES_${PN} += "${datadir}"

PNBLACKLIST[numberx] ?= "Runtime depends on blacklisted python-elementary"
