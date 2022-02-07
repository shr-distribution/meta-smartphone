DESCRIPTION = "Sówka libphone-ui-shr theme"
AUTHOR = "Sebastian Krzyszkowiak <dos@dosowisko.net>"
SECTION = "e/utils"
LICENSE = "CC-BY-SA-2.5"
LIC_FILES_CHKSUM = "file://COPYING;md5=d4e4f10748f3146a089aaa23c9ade59b"
DEPENDS = "edje-native"
RDEPENDS_${PN} = "libphone-ui-shr"
RSUGGESTS_${PN} = "elementary-theme-sowka"
SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.1+gitr${SRCPV}"
PR = "r2"
inherit allarch update-alternatives

SRC_URI = "git://github.com/shr-distribution/shr-themes;protocol=https;branch=master"

S = "${WORKDIR}/git/phoneui-shr/${PN}"

do_compile() {
        ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/images -fd ${S}/images ${S}/sowka.edc -o ${S}/sowka.edj
}
do_install() {
        install -d ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/sowka.edj ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/config ${D}${datadir}/libphone-ui-shr/config.${PN}
}

ALTERNATIVE_${PN} = "libphone-ui-shr-config"
ALTERNATIVE_LINK_NAME[libphone-ui-shr-config] = "${datadir}/libphone-ui-shr/config"
ALTERNATIVE_PRIORITY[libphone-ui-shr-config] = "1"

FILES_${PN} = "${datadir}/libphone-ui-shr/"
