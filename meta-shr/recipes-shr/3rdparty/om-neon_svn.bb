DESCRIPTION = "Simple image viewer"
HOMEPAGE = "http://neon.projects.openmoko.org/"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "x11/applications"

DEPENDS = "edje-native python-native"

SRCREV = "68"
PV = "1.0.0+svnr${SRCPV}"
PR = "r6"

S = "${WORKDIR}/trunk"

SRC_URI = "svn://svn.projects.openmoko.org/svnroot/neon;module=trunk"

inherit distutils

FILES_${PN} += "${datadir}/neon ${datadir}/applications/neon.desktop ${datadir}/pixmaps"

RDEPENDS_${PN} += "python-textutils python-evas python-ecore python-edje"

do_compile_prepend() {
    sed -i "s/\/opt\/bin\/edje_cc -v/${@"${STAGING_BINDIR_NATIVE}".replace('/', '\/')}\/edje_cc/g" ${S}/build_edje.py
    sed -i "s/#THEMES_DIR = '\/usr\/share\/neon\/themes'/THEMES_DIR = '\/usr\/share\/neon\/themes'/g" ${S}/neon/neon.py
    sed -i "s/THEMES_DIR = '..\/data\/themes'/#THEMES_DIR = '..\/data\/themes'/g" ${S}/neon/neon.py
}

PNBLACKLIST[om-neon] ?= "Runtime depends on blacklisted python-evas - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[om-neon] ?= "Runtime depends on blacklisted om-neon-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[om-neon] ?= "Runtime depends on blacklisted python-edje - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[om-neon] ?= "Runtime depends on blacklisted python-ecore - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[om-neon] ?= "Runtime depends on blacklisted om-neon - the recipe will be removed on 2017-09-01 unless the issue is fixed"
