DESCRIPTION = "gry* elementary theme - a fast, shiny elementary theme"
SECTION = "e/utils"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
DEPENDS = "edje-native"
RRECOMMENDS_${PN} = "e-wm-theme-illume-gry"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"

SRCREV = "5ce42d9e1eaa0cb28d773ab90a87e29e6352e2c5"
PV = "0.9+gitr${SRCPV}"
inherit allarch

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git/elementary/${PN}"

do_compile() {
    ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/. -fd ${S}/. ${S}/gry.edc -o ${S}/gry.edj
}

do_install() {
    install -d ${D}${datadir}/elementary/themes/
    install -m 0644 ${S}/gry.edj ${D}${datadir}/elementary/themes/
}

FILES_${PN} = "${datadir}/elementary/themes/"
