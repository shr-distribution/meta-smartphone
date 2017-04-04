DESCRIPTION = "EFL Dictionary Viewer. It supports dictionaries in SDictionary and StarDict format."
SECTION = "devel/python"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
AUTHOR = "Luca Vaudano <vaudano@gmail.com>"
HOMEPAGE = "http://babiloo-project.org"
RDEPENDS_${PN} = "python-elementary python-compression python-misc python-netclient"

DEFAULT_PREFERENCE = "-1"
SRCREV = "296"
PV = "2.0.9-bzrr${SRCPV}"
PR = "r4"

SRC_URI = "bzr://bazaar.launchpad.net/~vaudano/babiloo/efl"

S = "${WORKDIR}/efl"

do_install() {
    install -d "${D}${datadir}/babiloo"
    cp -R --no-dereference --preserve=mode,links -v "${S}/core" "${D}${datadir}/babiloo/"
    cp -R --no-dereference --preserve=mode,links -v "${S}/efl" "${D}${datadir}/babiloo/"
    cp -R --no-dereference --preserve=mode,links -v "${S}/images" "${D}${datadir}/babiloo/"
    cp -R --no-dereference --preserve=mode,links -v "${S}/dicts" "${D}${datadir}/babiloo/"
    install -m 0755 "${S}/run.py" "${D}${datadir}/babiloo/"
    install -d "${D}${bindir}"
    ln -s "${datadir}/babiloo/run.py" "${D}${bindir}/babiloo"
    install -d "${D}${datadir}/pixmaps"
    install -m 0644 "${S}/images/babiloo.png" "${D}${datadir}/pixmaps"
    install -d "${D}${datadir}/applications"
    install -m 0644 "${S}/babiloo.desktop" "${D}${datadir}/applications"

    install -d "${D}${datadir}"
    cp -R --no-dereference --preserve=mode,links -v "${S}/locale" "${D}${datadir}/"
    find ${D}${datadir}/locale -name *.po -exec rm {} \;
    rm -f ${D}${datadir}/locale/babiloo.pot

    install -d "${D}${datadir}/doc"
    install -m 0644 "${S}/doc/efl/babiloo.pdf" "${D}${datadir}/doc/"
}

FILES_${PN} += "${datadir}/babiloo"

PNBLACKLIST[babiloo-efl] ?= "Runtime depends on blacklisted python-elementary - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[babiloo-efl] ?= "Runtime depends on blacklisted babiloo-efl - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[babiloo-efl] ?= "Runtime depends on blacklisted babiloo-efl-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"
