FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_athene = "${MACHINE_ARCH}"

SRC_URI_append_athene = " file://70-athene.rules"

do_install_append_athene() {
    install -m 0644 ${WORKDIR}/70-athene.rules ${D}${sysconfdir}/udev/rules.d/70-athene.rules
}
