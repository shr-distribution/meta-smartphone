DESCRIPTION = "gtkrc u-a for clearlooks gtk theme"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

RDEPENDS_${PN} = "gnome-icon-theme gtk-theme-clearlooks"

SRC_URI += "file://gtkrc"

do_install() {
        install -d ${D}${sysconfdir}/gtk-2.0/
        install -m 0644 ${WORKDIR}/gtkrc ${D}${sysconfdir}/gtk-2.0/gtkrc.${PN}
}

FILES_${PN} = "${sysconfdir}/gtk-2.0/gtkrc.${PN}"

pkg_postinst_${PN} () {
        update-alternatives --install ${sysconfdir}/gtk-2.0/gtkrc gtk-theme ${sysconfdir}/gtk-2.0/gtkrc.${PN} 20
}
pkg_prerm_${PN} () {
        update-alternatives --remove gtk-theme ${sysconfdir}/gtk-2.0/gtkrc.${PN}
}

PNBLACKLIST[gtk-theme-clearlooks-gtkrc] ?= "Runtime depends on blacklisted gtk-theme-clearlooks"

PNBLACKLIST[gtk-theme-clearlooks-gtkrc] ?= "Runtime depends on blacklisted gtk-theme-clearlooks-gtkrc-dev"
