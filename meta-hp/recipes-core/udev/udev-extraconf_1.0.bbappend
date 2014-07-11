FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH_tenderloin = "${MACHINE_ARCH}"

SRC_URI_append_tenderloin = " file://tenderloin.rules"

do_install_append_tenderloin() {
    install -m 0644 ${WORKDIR}/tenderloin.rules ${D}${sysconfdir}/udev/rules.d/tenderloin.rules
}
