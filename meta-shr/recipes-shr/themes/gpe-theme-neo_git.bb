DESCRIPTION = "nEo GPE theme - a very fast, high contrast GPE theme"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
RSUGGESTS_${PN} = "gpe-filemanager gpe-sketchbook elementary-theme-neo e-wm-theme-illume-neo gtk-theme-neo icon-theme-neo"
LICENSE = "CC-BY-SA-2.5"
LIC_FILES_CHKSUM = "file://../COPYING;md5=d4e4f10748f3146a089aaa23c9ade59b"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.3+gitr${SRCPV}"
PR = "${INC_PR}.4"

SRC_URI = "git://github.com/shr-distribution/shr-themes;protocol=https;branch=master"

S = "${WORKDIR}/git/gpe/theme-neo/pixmaps"

require gpe-icons.inc

ALTERNATIVE_PRIORITY = "10"

#installed to /usr/share/gpe/pixmaps.gpe-theme-neo by default as we don't want to overwrite default icons from gpe-icons package
do_install() {
        install -d ${D}${datadir}/gpe/
        install -d ${D}${datadir}/gpe/pixmaps.${PN}/
        cp -R ${S}/* "${D}${datadir}/gpe/pixmaps.${PN}/"
}

FILES_${PN} = "${datadir}/gpe/pixmaps.${PN}/"
