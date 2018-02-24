FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_tenderloin = "${MACHINE_ARCH}"

SRC_URI_append_tenderloin = " file://70-tenderloin.rules"

do_install_append_tenderloin() {
    install -m 0644 ${WORKDIR}/70-tenderloin.rules ${D}${sysconfdir}/udev/rules.d/70-tenderloin.rules
}
