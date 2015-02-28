DESCRIPTION = "Gwaterpas"
AUTHOR   = "Kurt Van Dijck"
SECTION = "Utility"
HOMEPAGE = "http://wiki.openmoko.org/wiki/Gwaterpas"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://main.cc;beginline=5;endline=9;md5=793da126c0859ce50ca81b7b245396bc"
DEPENDS = "fltk"

SRC_URI = "http://www.ecirc.net/openmoko/${P}.tgz \
           file://sensors_fix.patch;apply=yes \
           file://remove_buildflags.patch;apply=yes \
           file://desktop_fix.patch;apply=yes \
"

PNBLACKLIST[gwaterpas] ?= "broken: depends on blacklisted fltk"

SRC_URI[md5sum] = "b3be847e3f89dbbcdca3e0184686bcef"
SRC_URI[sha256sum] = "23d56f10089722a33cde507cd398c82871dd14b80be1bc3f4cd2daee2cfcf641"

do_configure_prepend() {
  #fix DSO
  sed -i 's/^LDLIBS.*/LDLIBS = -lfltk -lstdc++ -lm -lfontconfig -lX11 -lXft -lXext/' ${S}/Makefile
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/gwaterpas ${D}${bindir}
    install -d ${D}/${datadir}/applications
    install -m 0644 ${S}/desktop ${D}/${datadir}/applications/gwaterpas.desktop
    install -d ${D}/${datadir}/gwaterpas
    install -m 0644 ${S}/icon.png ${D}/${datadir}/gwaterpas/icon.png
}
