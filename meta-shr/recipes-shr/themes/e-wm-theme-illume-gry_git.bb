DESCRIPTION = "gry* illume theme - a fast, shiny illume theme"
SECTION = "e/utils"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
DEPENDS = "edje-native"
RRECOMMENDS_${PN} = "elementary-theme-gry"
inherit allarch
LICENSE = "MIT & BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"

SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.4+gitr${SRCPV}"

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git/e-wm/${PN}"

do_compile() {
    ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/. -fd ${S}/. ${S}/gry.edc -o ${S}/gry.edj
}

do_install() {
    install -d ${D}${datadir}/enlightenment/data/themes/
    install -m 0644 ${S}/gry.edj ${D}${datadir}/enlightenment/data/themes/
}

FILES_${PN} = "${datadir}/enlightenment/data/themes/"
