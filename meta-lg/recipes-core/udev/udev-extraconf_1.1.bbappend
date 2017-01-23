FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_mako = "${MACHINE_ARCH}"
PACKAGE_ARCH_hammerhead = "${MACHINE_ARCH}"

SRC_URI_append_mako = " file://mako.rules"
SRC_URI_append_hammerhead = " file://hammerhead.rules"

do_install_append_mako() {
    install -m 0644 ${WORKDIR}/mako.rules ${D}${sysconfdir}/udev/rules.d/mako.rules
}

do_install_append_hammerhead() {
    install -m 0644 ${WORKDIR}/hammerhead.rules ${D}${sysconfdir}/udev/rules.d/hammerhead.rules
}
