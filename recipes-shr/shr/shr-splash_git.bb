DESCRIPTION = "SHR splash screen"
SECTION = "x11/data"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://${WORKDIR}/LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "0375bf2b66a053dd490774004c56b5d949f02ac8"
PV = "1.2+gitr${SRCPV}"
PR = "r7"
SHR_SPLASH_THEME ?= "shr-splash-theme-logo"
DEPENDS = "${SHR_SPLASH_THEME}"
RRECOMMENDS_${PN} = "${SHR_SPLASH_THEME}"

inherit update-rc.d gettext

SRC_URI = "git://git.shr-project.org/repo/shr.git;protocol=http;branch=master \
           file://LICENSE"

S = "${WORKDIR}/git/${PN}"

FILES_${PN} = "${datadir}/shr-splash \
	       ${sysconfdir}/init.d/shr-splash.sh \
"

do_install() {
    install -d ${D}${datadir}/shr-splash
    install -d ${D}${datadir}/shr-splash/themes
    install -d ${D}${sysconfdir}/init.d

    install -m 0755 ${S}/shr-splash.sh        ${D}${sysconfdir}/init.d/shr-splash.sh

}

pkg_postinst() {
    [ -e ${datadir}/pixmaps/xsplash-vga.ppm ] || ln -s ${datadir}/shr-splash/theme/xsplash-vga.ppm ${datadir}/pixmaps/xsplash-vga.ppm
}


INITSCRIPT_NAME = "shr-splash.sh"
INITSCRIPT_PARAMS = "start 01 S . stop 21 0 1 6 ."
