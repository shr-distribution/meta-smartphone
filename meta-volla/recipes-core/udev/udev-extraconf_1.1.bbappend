FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_yggdrasil   = "${MACHINE_ARCH}"

SRC_URI_append_yggdrasil   = " file://70-yggdrasil.rules"

do_install_append_yggdrasil() {
    install -m 0644 ${WORKDIR}/70-yggdrasil.rules ${D}${sysconfdir}/udev/rules.d/70-yggdrasil.rules
}
