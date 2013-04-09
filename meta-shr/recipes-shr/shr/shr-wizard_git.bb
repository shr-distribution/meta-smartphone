DESCRIPTION = "An e17 module and a python app for the SHR first run wizard"
HOMEPAGE = "http://shr-project.org"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "e-wm"
RDEPENDS_${PN} = "python-elementary shr-settings python-phoneutils e-wm python-dbus python-edbus psmisc"
SECTION = "x11/application"
SRCREV = "1dae79221f07d3cf78d11a32804326f8daf7e59e"
PV = "0.0.0+gitr${SRCPV}"
PR = "r13"

inherit autotools

SRC_URI = "git://git.shr-project.org/repo/shr-wizard.git;protocol=http;branch=master"
S = "${WORKDIR}/git"

FILES_${PN} += "${libdir}/enlightenment/modules/wizard/*/page_900.so"
FILES_${PN}-dev += "${libdir}/enlightenment/modules/wizard/*/page_900.la"
FILES_${PN}-staticdev += "${libdir}/enlightenment/modules/wizard/*/page_900.a"
FILES_${PN}-dbg += "${libdir}/enlightenment/modules/wizard/*/.debug/"
