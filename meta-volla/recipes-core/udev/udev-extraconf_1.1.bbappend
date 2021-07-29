FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PACKAGE_ARCH:yggdrasil   = "${MACHINE_ARCH}"

SRC_URI:append:yggdrasil   = " file://70-yggdrasil.rules"

do_install:append:yggdrasil() {
    install -m 0644 ${WORKDIR}/70-yggdrasil.rules ${D}${sysconfdir}/udev/rules.d/70-yggdrasil.rules
}
