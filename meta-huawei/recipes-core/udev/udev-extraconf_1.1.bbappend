FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:angler = "${MACHINE_ARCH}"

SRC_URI:append:angler = " file://70-angler.rules"

do_install:append:angler() {
    install -m 0644 ${WORKDIR}/70-angler.rules ${D}${sysconfdir}/udev/rules.d/70-angler.rules
}
