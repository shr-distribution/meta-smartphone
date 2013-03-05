DESCRIPTION = "Simple helper utilitiy to start binaries from a bionic based Android \
in a glibc based linux environment"
AUTHOR = "Simon Busch <morphis@gravedo.de>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "file://android-exec"

do_install() {
    install -d ${D}${bindir}
    install -m 755 ${WORKDIR}/android-exec ${D}${bindir}/
}

PACKAGES = "${PN}"
