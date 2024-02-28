FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:mindphone = "${MACHINE_ARCH}"

SRC_URI:append:mindphone = " file://70-mindphone.rules"

do_install:append:mindphone() {
    install -m 0644 ${WORKDIR}/70-mindphone.rules ${D}${sysconfdir}/udev/rules.d/70-mindphone.rules
}
