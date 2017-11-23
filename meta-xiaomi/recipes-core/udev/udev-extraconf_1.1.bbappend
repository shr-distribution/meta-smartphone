FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_mido = "${MACHINE_ARCH}"

SRC_URI_append_mido = " file://mido.rules"

do_install_append_mido() {
    install -m 0644 ${WORKDIR}/mido.rules ${D}${sysconfdir}/udev/rules.d/mido.rules
}
