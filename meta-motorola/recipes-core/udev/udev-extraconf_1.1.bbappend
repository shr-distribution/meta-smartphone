FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:athene = "${MACHINE_ARCH}"

SRC_URI:append:athene = " file://70-athene.rules"

do_install:append:athene() {
    install -m 0644 ${WORKDIR}/70-athene.rules ${D}${sysconfdir}/udev/rules.d/70-athene.rules
}
