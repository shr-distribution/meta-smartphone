FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_mido   = "${MACHINE_ARCH}"
PACKAGE_ARCH_oxygen   = "${MACHINE_ARCH}"
PACKAGE_ARCH_rosy   = "${MACHINE_ARCH}"
PACKAGE_ARCH_sagit   = "${MACHINE_ARCH}"
PACKAGE_ARCH_tissot = "${MACHINE_ARCH}"

SRC_URI_append_mido   = " file://70-mido.rules"
SRC_URI_append_oxygen   = " file://70-oxygen.rules"
SRC_URI_append_rosy   = " file://70-rosy.rules"
SRC_URI_append_sagit = " file://70-sagit.rules"
SRC_URI_append_tissot = " file://70-tissot.rules"


do_install_append_mido() {
    install -m 0644 ${WORKDIR}/70-mido.rules ${D}${sysconfdir}/udev/rules.d/70-mido.rules
}

do_install_append_oxygen() {
    install -m 0644 ${WORKDIR}/70-oxygen.rules ${D}${sysconfdir}/udev/rules.d/70-oxygen.rules
}

do_install_append_rosy() {
    install -m 0644 ${WORKDIR}/70-rosy.rules ${D}${sysconfdir}/udev/rules.d/70-rosy.rules
}

do_install_append_sagit() {
    install -m 0644 ${WORKDIR}/70-sagit.rules ${D}${sysconfdir}/udev/rules.d/70-sagit.rules
}

do_install_append_tissot() {
    install -m 0644 ${WORKDIR}/70-tissot.rules ${D}${sysconfdir}/udev/rules.d/70-tissot.rules
}
