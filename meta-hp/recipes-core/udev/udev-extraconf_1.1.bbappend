FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:tenderloin = "${MACHINE_ARCH}"

SRC_URI:append:tenderloin = " file://70-tenderloin.rules"

do_install:append:tenderloin() {
    install -m 0644 ${WORKDIR}/70-tenderloin.rules ${D}${sysconfdir}/udev/rules.d/70-tenderloin.rules
}
