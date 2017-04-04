DESCRIPTION = "PISI is synchronizing information"
AUTHOR = "Michael Pilgermann"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
HOMEPAGE = "http://freshmeat.net/projects/pisiom"
SRCNAME = "pisi"
DEPENDS = "python-native"
RDEPENDS_${PN} = "python-vobject python-core python-pygtk python-pycairo\
           python-gdata python-webdav python-ldap python-epydoc python-core\
           python-dateutil python-sqlite3 python-netserver python-netclient\
           python-misc python-ctypes libsyncml"

# python-pygobject is broken http://lists.openembedded.org/pipermail/openembedded-devel/2016-June/107798.html
# RDEPENDS_${PN} += "python-pygobject"

# Rdepends on libsyncml and python-pygtk
#PNBLACKLIST[pisi] ?= "${@bb.utils.contains('DISTRO_FEATURES', 'bluez5', 'bluez5 conflicts with bluez4 and bluez5 is selected in DISTRO_FEATURES', '', d)}"
PNBLACKLIST[pisi] ?= "RDEPENDS on python-pygtk which was removed from oe-core - the recipe will be removed on 2017-09-01 unless the issue is fixed"

SRC_URI = "http://github.com/downloads/kichkasch/pisi/pisi-src-${PV}.tar.gz"
SRC_URI[md5sum] = "c416b316668575f8506dc54e19475795"
SRC_URI[sha256sum] = "a27603662747aee9a0440acc6472fe274c0724fcc66ad05849eb186bfb24868e"

PR = "r2"
inherit pythonnative

# we want conf.example in $PN
FILES_${PN}-doc = ""
FILES_${PN} += "/opt/${PN} \
                ${datadir}/pixmaps \
                ${datadir}/applications \
                ${datadir}/doc/${PN}"
CONFFILES_${PN} += "/usr/share/doc/${PN}/conf.example"

do_compile() {
    ${PYTHON} ${S}/setup.py build ${D}
}

do_install() {
    ${PYTHON} ${S}/setup.py install ${D}
    rm -rf ${D}/opt/pisi/build/
    rm -rf ${D}/opt/pisi/patches/
}
