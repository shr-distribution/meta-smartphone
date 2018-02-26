FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_angler = "${MACHINE_ARCH}"

SRC_URI_append_angler = " file://70-angler.rules"

do_install_append_angler() {
    install -m 0644 ${WORKDIR}/70-angler.rules ${D}${sysconfdir}/udev/rules.d/70-angler.rules
}
