DESCRIPTION = "Numpty Physics is a drawing puzzle game in the spirit (and style?) \
of Crayon Physics using the same excellent Box2D engine."
DEPENDS = "virtual/libsdl libsdl-image libpng glib-2.0 dbus"
RDEPENDS_${PN} += "libpng"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
HOMEPAGE = "http://numptyphysics.garage.maemo.org/"
SECTION = "x11/games"
SRCREV = "109"
PV = "0.2+svnr${SRCPV}"
PR = "r6"

inherit autotools-brokensep pkgconfig

# Maemo garage is sadly only available with https. Can make you trouble while fetching without accepting the certificate.
SRC_URI = "\
  svn://vcs.maemo.org/svn/${PN};module=trunk;protocol=http \
  http://wwwpub.zih.tu-dresden.de/~mkluge/numptyphysics_setup.tgz \
  file://replay_off.patch;striplevel=0 \
  file://next.png \
  file://keyb.patch;striplevel=0 \
  file://keyb.png \
  file://faster.patch;striplevel=0 \
  file://numptyphysics.desktop \
  file://gcc-4.7.patch \
  file://configure.patch \
"
S = "${WORKDIR}/trunk"

EXTRA_S = "${WORKDIR}/local/packages/numptyphysics"

do_configure_append() {
  cp -R --no-dereference --preserve=mode,links -v ../next.png data
  cp -R --no-dereference --preserve=mode,links -v ../keyb.png data
}

do_install_append() {
        install -d ${D}${datadir}/numptyphysics
        install -d ${D}${datadir}/pixmaps
        install -d ${D}${datadir}/applications
        install -m 0644 ${EXTRA_S}/star.png ${D}${datadir}/pixmaps
        install -m 0644 ../numptyphysics.desktop ${D}/${datadir}/applications
        cp -R --no-dereference --preserve=mode,links -v ${EXTRA_S}/data/* data/keyb.png ${D}/${datadir}/numptyphysics/
}

FILES_${PN} += "${datadir}"

SRC_URI[md5sum] = "24a031f628cc91825bb80ee2aaa21b77"
SRC_URI[sha256sum] = "3a60d6a6327f3ad8653e1de5dbf5c36f2a591d409d3e3ae7f6c84d25dbc3e7da"
