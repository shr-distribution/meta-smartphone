DESCRIPTION = "Embedded linux APRS tool"
HOMEPAGE = "http://code.google.com/p/atrack/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
AUTHOR = "Petr Vanek <vanous@penguin.cz>"
SECTION = "x11/applications"

SRCREV = "128"
PV = "0.0.81+svnr${SRCPV}"
PR = "r2"

SRC_URI = "svn://atrack.googlecode.com/svn;module=trunk;proto=http"
S = "${WORKDIR}/trunk"

inherit distutils

FILES_${PN} += "${datadir}/atrack ${datadir}/applications/atrack.desktop ${datadir}/pixmaps"

RDEPENDS_${PN} += "python-netclient python-elementary"
