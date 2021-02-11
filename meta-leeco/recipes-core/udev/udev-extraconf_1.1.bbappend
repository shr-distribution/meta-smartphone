FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_s2   = "${MACHINE_ARCH}"

SRC_URI_append_s2   = " file://70-s2.rules"

do_install_append_s2() {
    install -m 0644 ${WORKDIR}/70-s2.rules ${D}${sysconfdir}/udev/rules.d/70-s2.rules
}
