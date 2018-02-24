FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_onyx = "${MACHINE_ARCH}"

SRC_URI_append_onyx = " file://70-onyx.rules"

do_install_append_onyx() {
    install -m 0644 ${WORKDIR}/70-onyx.rules ${D}${sysconfdir}/udev/rules.d/70-onyx.rules
}
