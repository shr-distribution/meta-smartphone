DESCRIPTION = "nEo icon theme - a high contrast icon theme which looks especially well when using all the other nEo themes"
SECTION = "e/utils"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
RDEPENDS_${PN} = "e-wm"
RSUGGESTS_${PN} = "elementary-theme-neo e-wm-theme-illume-neo gtk-theme-neo gpe-theme-neo"
LICENSE = "unknown"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.3+gitr${SRCPV}"
PR = "r4"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/icons/icon-theme-neo"

do_install() {
        install -d ${D}${datadir}/icons/
        install -d ${D}${datadir}/icons/nEo/
        cp -R ${S}/* "${D}${datadir}/icons/nEo/"
}

FILES_${PN} = "${datadir}/icons/nEo/"

pkg_postinst() {
        echo "To activate this theme select it under ICON THEME in the LOOK tab of enlightenment settings"
        echo "Restart enlightenment for the changes to take affect"
}
