FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:sargo   = "${MACHINE_ARCH}"

SRC_URI:append:sargo   = " file://70-sargo.rules"

do_install:append:sargo() {
    install -m 0644 ${WORKDIR}/70-sargo.rules ${D}${sysconfdir}/udev/rules.d/70-sargo.rules
}
