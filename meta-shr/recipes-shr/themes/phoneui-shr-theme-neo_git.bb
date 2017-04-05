DESCRIPTION = "nEo phoneui-shr theme - a very fast, high contrast phoneui-shr theme"
AUTHOR = "Jesus McCloud <bernd.pruenster@gmail.com"
HOMEPAGE = "http://jmccloud.jm.funpic.de"
SECTION = "e/utils"
LICENSE = "CC-BY-SA-2.5"
LIC_FILES_CHKSUM = "file://COPYING;md5=d4e4f10748f3146a089aaa23c9ade59b"
DEPENDS = "edje-native"
RDEPENDS_${PN} = "libphone-ui-shr"
RSUGGESTS_${PN} = "e-wm-theme-illume-neo gtk-theme-neo icon-theme-neo elementary-theme-neo"
SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.1+gitr${SRCPV}"
PR = "r3"
inherit allarch update-alternatives

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/phoneui-shr/${PN}"

do_compile() {
        ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/. -fd ${S}/. ${S}/neo.edc -o ${S}/neo.edj
        ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/idle_screen -fd ${S}/idle_screen ${S}/neo.edc -o ${S}/idle_screen.edj.neo
}
do_install() {
        install -d ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/neo.edj ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/idle_screen.edj.neo ${D}${datadir}/libphone-ui-shr/
        install -m 0644 ${S}/config ${D}${datadir}/libphone-ui-shr/config.${PN}
}

ALTERNATIVE_${PN} = "libphone-ui-shr-config"
ALTERNATIVE_LINK_NAME[libphone-ui-shr-config] = "${datadir}/libphone-ui-shr/config"
ALTERNATIVE_PRIORITY[libphone-ui-shr-config] = "4"

FILES_${PN} = "${datadir}/libphone-ui-shr/"

PNBLACKLIST[phoneui-shr-theme-neo] ?= "Runtime depends on blacklisted libphone-ui-shr - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[phoneui-shr-theme-neo] ?= "Runtime depends on blacklisted phoneui-shr-theme-neo-dev - the recipe will be removed on 2017-09-01 unless the issue is fixed"
