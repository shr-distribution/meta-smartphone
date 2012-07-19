DESCRIPTION = "Sówka elementary theme"
SECTION = "e/utils"
DEPENDS = "edje-native"
LICENSE = "MIT BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f523ab5986cc79b52a90d2ac3d5454a2"
SRCREV = "bbe2445c29cd65d0a8dddde7606434db489ee41b"
PV = "0.2+gitr${SRCPV}"
PR = "r1"
inherit allarch

SRC_URI = "git://git.shr-project.org/repo/shr-themes.git;protocol=http;branch=master"

S = "${WORKDIR}/git/elementary/${PN}"

do_compile() {
    ${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/images/. -fd ${S}/fonts/. ${S}/sowka.edc -o ${S}/sowka.edj
}

do_install() {
    install -d ${D}${datadir}/elementary/themes/
    install -m 0644 ${S}/sowka.edj ${D}${datadir}/elementary/themes/
}

FILES_${PN} = "${datadir}/elementary/themes/sowka.edj"
