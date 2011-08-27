DESCRIPTION = "An e17 module and a python app for the SHR first run wizard"
HOMEPAGE = "http://shr-project.org"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "e-wm"
RDEPENDS_${PN} = "python-elementary shr-settings python-phoneutils e-wm python-dbus python-edbus"
SECTION = "x11/application"
SRCREV = "94deff37e2f17dce0e7b4f51e16ee8e5981de58a"
PV = "0.0.0+gitr${SRCPV}"
PR = "r12"

inherit autotools

SRC_URI = "git://git.shr-project.org/repo/shr-wizard.git;protocol=http;branch=master"
SRC_URI += "file://volatiles-98-appshadow"
S = "${WORKDIR}/git"

do_install_append() {
        install -d ${D}${sysconfdir}/default/volatiles
        install -m 0644    ${WORKDIR}/volatiles-98-appshadow   ${D}${sysconfdir}/default/volatiles/98_appshadow
}

FILES_${PN} += "${libdir}/enlightenment/modules/wizard/*/page_900.so"
FILES_${PN} += "${sysconfdir}/default/volatiles/98_appshadow"
