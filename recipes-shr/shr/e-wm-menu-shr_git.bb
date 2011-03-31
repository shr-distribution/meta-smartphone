DESCRIPTION = "illume SHR applications.menu config"
SECTION = "e/utils"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"

SRCREV = "1cc80e26a4558dfc2268b349d9a1f468e515bcfb"
PV = "1.2+gitr${SRCPV}"
PR = "r2"
PACKAGE_ARCH = "all"

RCONFLICTS_${PN} = "e-wm-menu"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
           file://LICENSE \
"

S = "${WORKDIR}/git/e-wm/${PN}"

FILES_${PN} = "${sysconfdir}/xdg/menus/applications.menu"

do_install() {
    install -d ${D}${sysconfdir}/xdg/menus
    install -m 0755 ${S}/applications.menu ${D}${sysconfdir}/xdg/menus/applications.menu
}
