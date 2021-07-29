FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:onyx = "${MACHINE_ARCH}"

SRC_URI:append:onyx = " file://70-onyx.rules"

do_install:append:onyx() {
    install -m 0644 ${WORKDIR}/70-onyx.rules ${D}${sysconfdir}/udev/rules.d/70-onyx.rules
}
