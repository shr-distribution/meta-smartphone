DESCRIPTION = "A generic framework for phone ui"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"
SECTION = "libs"
SRCREV = "ca6d795a50fac3658a4ff6c7b3f160085df6a2c3"
PV = "0.0.1+gitr${SRCPV}"
PR = "r0"

DEPENDS="glib-2.0 libshr-glib libfso-glib libfsoframework libphone-utils alsa-lib"

SRC_URI = " \
 git://github.com/shr-distribution/libphone-ui;protocol=https;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools pkgconfig

RDEPENDS_${PN} = "${PN}-config"
