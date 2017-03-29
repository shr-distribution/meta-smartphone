DESCRIPTION = "elementary SHR theme"
SECTION = "e/utils"
DEPENDS = "edje-native"
LICENSE = "MIT & BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "4e6035ef5452b2f0a128dff91ddeb8335a3aafec"
PV = "0.2+gitr${SRCPV}"
inherit allarch

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/elementary/${PN}"

do_compile() {
    ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/images/. -fd ${S}/fonts/. ${S}/sixteen.edc -o ${S}/sixteen.edj
}

do_install() {
    install -d ${D}${datadir}/elementary/themes/
    install -m 0644 ${S}/sixteen.edj ${D}${datadir}/elementary/themes/
}

FILES_${PN} = "${datadir}/elementary/themes/sixteen.edj"



PNBLACKLIST[elementary-theme-sixteen] ?= "Depends on blacklisted edje-native"

PNBLACKLIST[elementary-theme-sixteen] ?= "Runtime depends on blacklisted elementary-theme-sixteen-dev"

PNBLACKLIST[elementary-theme-sixteen] ?= "Runtime depends on blacklisted elementary-theme-sixteen"
