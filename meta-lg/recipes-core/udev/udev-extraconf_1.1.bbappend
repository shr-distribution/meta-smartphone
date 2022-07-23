FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:mako = "${MACHINE_ARCH}"
PACKAGE_ARCH:hammerhead-halium = "${MACHINE_ARCH}"

SRC_URI:append:mako = " file://70-mako.rules"
SRC_URI:append:hammerhead-halium = " file://70-hammerhead-halium.rules"

do_install:append:mako() {
    install -m 0644 ${WORKDIR}/70-mako.rules ${D}${sysconfdir}/udev/rules.d/70-mako.rules
}

do_install:append:hammerhead-halium() {
    install -m 0644 ${WORKDIR}/70-hammerhead-halium.rules ${D}${sysconfdir}/udev/rules.d/70-hammerhead.rules
}
