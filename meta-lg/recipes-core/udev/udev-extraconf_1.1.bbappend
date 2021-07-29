FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:mako = "${MACHINE_ARCH}"
PACKAGE_ARCH:hammerhead = "${MACHINE_ARCH}"

SRC_URI:append:mako = " file://70-mako.rules"
SRC_URI:append:hammerhead = " file://70-hammerhead.rules"

do_install:append:mako() {
    install -m 0644 ${WORKDIR}/70-mako.rules ${D}${sysconfdir}/udev/rules.d/70-mako.rules
}

do_install:append:hammerhead() {
    install -m 0644 ${WORKDIR}/70-hammerhead.rules ${D}${sysconfdir}/udev/rules.d/70-hammerhead.rules
}
