DESCRIPTION = "o2 theme for libphone-ui-shr - it looks like om2007.2"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
SECTION = "e/utils"
LICENSE = "CC-BY-SA-2.5"
LIC_FILES_CHKSUM = "file://COPYING;md5=d4e4f10748f3146a089aaa23c9ade59b"
DEPENDS = "edje-native"
RDEPENDS_${PN} = "libphone-ui-shr"
RSUGGESTS_${PN} = "elementary-theme-o2"
SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.1+gitr${SRCPV}"
PR = "r3"
inherit allarch update-alternatives

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/phoneui-shr/${PN}"

do_compile() {
        ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/. -fd ${S}/. ${S}/o2.edc -o ${S}/o2.edj
}
do_install() {
        install -d ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/o2.edj ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/config ${D}${datadir}/libphone-ui-shr/config.${PN}
}

ALTERNATIVE_${PN} = "libphone-ui-shr-config"
ALTERNATIVE_LINK_NAME[libphone-ui-shr-config] = "${datadir}/libphone-ui-shr/config"
ALTERNATIVE_PRIORITY[libphone-ui-shr-config] = "3"

FILES_${PN} = "${datadir}/libphone-ui-shr/"

PNBLACKLIST[phoneui-shr-theme-o2] ?= "Runtime depends on blacklisted libphone-ui-shr"
