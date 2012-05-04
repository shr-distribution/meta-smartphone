DESCRIPTION = "Extra GTA04 udev rules"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

inherit allarch

INHIBIT_DEFAULT_DEPS = "1"

SRC_URI = " \
    file://charger.rules \
    file://hso.rules \
    file://input.rules \
"

do_install() {
    install -d ${D}${sysconfdir}/udev/rules.d

    install -m 0644 ${WORKDIR}/charger.rules     ${D}${sysconfdir}/udev/rules.d/charger.rules
    install -m 0644 ${WORKDIR}/hso.rules         ${D}${sysconfdir}/udev/rules.d/hso.rules
    install -m 0644 ${WORKDIR}/input.rules       ${D}${sysconfdir}/udev/rules.d/input.rules
}

FILES_${PN} = "${sysconfdir}/udev"
RDEPENDS_${PN} = "udev"
