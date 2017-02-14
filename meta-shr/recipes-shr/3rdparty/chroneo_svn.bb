DESCRIPTION = "A Stopwatch and Timer application"
HOMEPAGE = "http://code.google.com/p/chroneo/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://chroneo/chroneo.py;endline=21;md5=f5b4b0084ac20093c13ca27a5c50ce23"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "x11/applications"

DEPENDS = "python-native edje-native"

SRCREV = "9"
PV = "1.0.2+svnr${SRCPV}"
PR = "r2"

SRC_URI = "svn://chroneo.googlecode.com/svn;module=trunk;protocol=http \
  file://api_changes.patch \
"
S = "${WORKDIR}/trunk"

inherit distutils

FILES_${PN} += "${datadir}/chroneo ${datadir}/applications/chroneo.desktop ${datadir}/pixmaps/chroneo.png"

RDEPENDS_${PN} += "python-audio python-pyalsaaudio python-elementary python-sqlite3"

do_compile_prepend() {
    ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/data ${S}/data/chroneo.edc
}

PNBLACKLIST[chroneo] ?= "Runtime depends on blacklisted python-elementary"
