DESCRIPTION = "A generic framework for phone ui"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"
SECTION = "libs"
SRCREV = "ca6d795a50fac3658a4ff6c7b3f160085df6a2c3"
PV = "0.0.1+gitr${SRCPV}"

DEPENDS="glib-2.0 libshr-glib libfso-glib libfsoframework libphone-utils alsa-lib libx11"

SRC_URI = " \
 git://git.shr-project.org/repo/libphone-ui.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools pkgconfig

RDEPENDS_${PN} = "${PN}-config"
