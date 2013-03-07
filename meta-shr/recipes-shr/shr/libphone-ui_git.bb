DESCRIPTION = "A generic framework for phone ui"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"
SECTION = "libs"
SRCREV = "7b8fb8ec1a1e80342b5e6e62e9db49568a0405aa"
PV = "0.0.1+gitr${SRCPV}"
PR = "r1"

DEPENDS="glib-2.0 libshr-glib libfso-glib libfsoframework libphone-utils alsa-lib"

SRC_URI = " \
 git://git.shr-project.org/repo/libphone-ui.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools pkgconfig

RDEPENDS_${PN} = "${PN}-config"
