FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_mido = "${MACHINE_ARCH}"
PACKAGE_ARCH_rosy = "${MACHINE_ARCH}"

SRC_URI_append_mido = " file://70-mido.rules"
SRC_URI_append_rosy = " file://70-rosy.rules"

do_install_append_mido() {
    install -m 0644 ${WORKDIR}/70-mido.rules ${D}${sysconfdir}/udev/rules.d/70-mido.rules
}
do_install_append_rosy() {
    install -m 0644 ${WORKDIR}/70-rosy.rules ${D}${sysconfdir}/udev/rules.d/70-rosy.rules
}
