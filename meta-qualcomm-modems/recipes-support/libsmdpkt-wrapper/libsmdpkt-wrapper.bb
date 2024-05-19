SUMMARY = "Wrapper to get ofonod and qmicli working on smdpkt devices (MSM kernel's SMD)"
HOMEPAGE = "https://github.com/scintill/libsmdpkt_wrapper"
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

DEPENDS = "util-linux"

PV = "0.2"

SRC_URI = "git://github.com/scintill/libsmdpkt_wrapper.git;protocol=https;branch=master"

SRCREV = "1b951eca8a6d2690f6ce146d5f92338e071b7156"
S = "${WORKDIR}/git"

EXTRA_OEMAKE = "LDFLAGS='${LDFLAGS}'"

do_configure() {
    rm -f ${S}/libsmdpkt_wrapper.so
}

do_compile() {
    ${CC} -shared -fPIC -Wall -Wextra -Werror -std=c99 -O2 ${LDFLAGS} wrapper.c -o libsmdpkt_wrapper.so
}

do_install() {
    mkdir -p ${D}${libdir}/preload
    install -Dm644 ${S}/libsmdpkt_wrapper.so ${D}${libdir}/preload/libsmdpkt_wrapper.so
}

FILES:${PN} = "${libdir}/preload/libsmdpkt_wrapper.so"
