FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:s2   = "${MACHINE_ARCH}"

SRC_URI:append:s2   = " file://70-s2.rules"

do_install:append:s2() {
    install -m 0644 ${WORKDIR}/70-s2.rules ${D}${sysconfdir}/udev/rules.d/70-s2.rules
}
