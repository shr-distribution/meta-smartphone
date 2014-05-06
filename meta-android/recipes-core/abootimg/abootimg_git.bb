SUMMARY = "A tool to read/write/update android boot images"
HOMEPAGE = "https://gitorious.org/ac100/abootimg"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS = "util-linux"

PV = "0.6+gitr${SRCPV}"

SRC_URI = "git://gitorious.org/ac100/abootimg.git;protocol=git;branch=master"
SRCREV = "7e127fee6a3981f6b0a50ce9910267cd501e09d4"
S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/abootimg ${D}${bindir}
    install -m 0755 ${S}/abootimg-pack-initrd ${D}${bindir}
    install -m 0755 ${S}/abootimg-unpack-initrd ${D}${bindir}
}
