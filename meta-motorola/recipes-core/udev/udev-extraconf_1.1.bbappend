FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_athene = "${MACHINE_ARCH}"

SRC_URI_append_athene = " file://athene.rules"

do_install_append_athene() {
    install -m 0644 ${WORKDIR}/athene.rules ${D}${sysconfdir}/udev/rules.d/athene.rules
}
