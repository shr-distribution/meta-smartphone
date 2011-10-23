DESCRIPTION = "Qt-based DBus bindings for freesmartphone.org"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
SECTION = "devel"
DEPENDS = "fso-specs qfsodbusxml2cpp-native qfsodbusxml2cpp qt4-x11-free"
SRCREV = "1e164553baf077bf0250067dc53b66218cb01427"
PV = "2011.09.22.1+gitr${SRCPV}"
INC_PR = "r0"

inherit autotools pkgconfig

SRC_URI = "${FREESMARTPHONE_GIT}/libfso-qt.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

do_configure_prepend() {
  sed -i -e s#FSO_SPECS_DIR="#FSO_SPECS_DIR="${STAGING_DIR_HOST}/#g ${S}/configure.ac
}
