FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:mido   = "${MACHINE_ARCH}"
PACKAGE_ARCH:oxygen   = "${MACHINE_ARCH}"
PACKAGE_ARCH:rosy   = "${MACHINE_ARCH}"
PACKAGE_ARCH:sagit   = "${MACHINE_ARCH}"
PACKAGE_ARCH:tissot = "${MACHINE_ARCH}"

SRC_URI:append:mido   = " file://70-mido.rules"
SRC_URI:append:oxygen   = " file://70-oxygen.rules"
SRC_URI:append:rosy   = " file://70-rosy.rules"
SRC_URI:append:sagit = " file://70-sagit.rules"
SRC_URI:append:tissot = " file://70-tissot.rules"


do_install:append:mido() {
    install -m 0644 ${WORKDIR}/70-mido.rules ${D}${sysconfdir}/udev/rules.d/70-mido.rules
}

do_install:append:oxygen() {
    install -m 0644 ${WORKDIR}/70-oxygen.rules ${D}${sysconfdir}/udev/rules.d/70-oxygen.rules
}

do_install:append:rosy() {
    install -m 0644 ${WORKDIR}/70-rosy.rules ${D}${sysconfdir}/udev/rules.d/70-rosy.rules
}

do_install:append:sagit() {
    install -m 0644 ${WORKDIR}/70-sagit.rules ${D}${sysconfdir}/udev/rules.d/70-sagit.rules
}

do_install:append:tissot() {
    install -m 0644 ${WORKDIR}/70-tissot.rules ${D}${sysconfdir}/udev/rules.d/70-tissot.rules
}
