DESCRIPTION = "Enables dconf to be used with GSettings schemas from Qt (particularly from \
QML) applications."
AUTHOR = "Canonical Limitied"
HOMEPAGE = "http://www.canonical.com/"
SECTION = "qt4"

DEPENDS = "dconf"

LICENSE = "LGPL-3.0 & GPL-3.0"
LIC_FILES_CHKSUM = " \
  file://COPYING-LGPL3;md5=6a6a8e020838b23406c81b19c1d46df6 \
  file://COPYING-GPL3;md5=f27defe1e96c2e1ecd4e0c9be8967949"

PV = "0.0.0+gitr${SRCPV}"
PR = "r2"

SRC_URI = "git://gitorious.org/dconf-qt/dconf-qt.git;protocol=git;branch=master"
S = "${WORKDIR}/git"
SRCREV = "8df1eeaca6ee5939bf3bb9fc0ace20c60aa8752e"

inherit qt4x11 cmake

PACKAGES = "${PN}-dbg ${PN}-dev ${PN}"
RDEPENDS_${PN} = "glib-2.0-utils"
FILES_${PN} += " \
  ${libdir}/qt4/imports/QConf/libdconf-qml.so \
  ${libdir}/qt4/imports/QConf/qmldir"
FILES_${PN}-dbg += "${libdir}/qt4/imports/QConf/.debug"
