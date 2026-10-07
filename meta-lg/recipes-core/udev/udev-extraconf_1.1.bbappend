FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:hammerhead-halium = "${MACHINE_ARCH}"

SRC_URI:append:hammerhead-halium = " file://70-hammerhead-halium.rules"

do_install:append:hammerhead-halium() {
    install -m 0644 ${UNPACKDIR}/70-hammerhead-halium.rules ${D}${sysconfdir}/udev/rules.d/70-hammerhead.rules
}

PACKAGE_ARCH:mako-halium = "${MACHINE_ARCH}"

SRC_URI:append:mako-halium = " file://70-mako-halium.rules"

do_install:append:mako-halium() {
    install -m 0644 ${UNPACKDIR}/70-mako-halium.rules ${D}${sysconfdir}/udev/rules.d/70-mako.rules
}
