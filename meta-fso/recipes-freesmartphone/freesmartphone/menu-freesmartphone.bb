DESCRIPTION = "XDG menu file for the freesmartphone.org project"
LICENSE = "PD"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/PD;md5=b3597d12946881e13cb3b548d1173851"
PV = "0.1"
PR = "r2"

SRC_URI = "file://applications.menu"

do_install() {
    install -d ${D}/${sysconfdir}/xdg/menus
    install -m 644 ${WORKDIR}/applications.menu ${D}/${sysconfdir}/xdg/menus/
}

RCONFLICTS_${PN} = "e-wm-menu"
CONFFILES_${PN} = "${sysconfdir}/xdg/menus/applications.menu"
FILES_${PN} = "${sysconfdir}/xdg/menus/applications.menu"
