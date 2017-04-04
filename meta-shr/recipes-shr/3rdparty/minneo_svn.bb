DESCRIPTION = "A classic Memory game for mobile devices"
HOMEPAGE = "http://code.google.com/p/minneo/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "x11/applications"

DEPENDS = "python-native edje-native"

SRCREV = "6"
PV = "1.0.2+svnr${SRCPV}"
PR = "r2"

SRC_URI = "svn://minneo.googlecode.com/svn;module=trunk;protocol=http \
           file://setup.py.patch"
S = "${WORKDIR}/trunk"

inherit distutils

FILES_${PN} += "${datadir}/minneo ${datadir}/applications/minneo.desktop ${datadir}/pixmaps/minneo.png"

RDEPENDS_${PN} += "python-audio python-pyalsaaudio python-elementary"

do_compile_append() {
        cd ${S}/data/themes
        ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/data/themes/default ${S}/data/themes/default/minneo.edc
}

PNBLACKLIST[minneo] ?= "Runtime depends on blacklisted python-elementary - the recipe will be removed on 2017-09-01 unless the issue is fixed"
