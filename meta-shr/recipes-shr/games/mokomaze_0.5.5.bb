DESCRIPTION = "Classic game where you control a steel ball by tilting a wooden labyrinth"
HOMEPAGE = "http://mokomaze.projects.openmoko.org/"
SECTION = "x11/games"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
DEPENDS = "libsdl-ttf libsdl-image ode intltool-native glib-2.0"
RDEPENDS_${PN} = "ttf-liberation-mono libpng"
RRECOMMENDS_${PN} = "fsoraw"
PV = "0.5.5+git8"

SRC_URI = "http://mokomaze.projects.openmoko.org/files/${PN}-${PV}.tar.gz \
           file://fsoraw.patch \
           file://0001-Fix-accelerometers-with-2.6.34-kernels.patch \
           file://0001-Fix-vibration-on-gta02-with-2.6.34-kernel.patch \
           file://use.pkg-config.for.ode.patch \
"
SRC_URI[md5sum] = "f4e1dbd444b4049c361f9c1c3d40d32b"
SRC_URI[sha256sum] = "515d842b79a2c34f5789fa10110bc9d7b15c65a7b1fa623131a1e03599fe7362"

inherit autotools pkgconfig gettext

EXTRA_OECONF = "FONTDIR=${datadir}/fonts/truetype --enable-rgb-swap"
