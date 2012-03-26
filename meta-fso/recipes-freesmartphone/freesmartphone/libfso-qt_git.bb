DESCRIPTION = "Qt-based DBus bindings for freesmartphone.org"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
SECTION = "devel"
DEPENDS = "fso-specs qfsodbusxml2cpp qfsodbusxml2cpp-native qt4-x11-free"
SRCREV = "9d77e2c61efbf3c9123bd2c7c26f9173588de669"
PV = "2012.02.16.1+gitr${SRCPV}"
INC_PR = "r1"

inherit autotools pkgconfig

SRC_URI = "${FREESMARTPHONE_GIT}/libfso-qt.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

do_configure_prepend() {
  sed -i -e s#FSO_SPECS_DIR="#FSO_SPECS_DIR="${STAGING_DIR_HOST}/#g ${S}/configure.ac
}
