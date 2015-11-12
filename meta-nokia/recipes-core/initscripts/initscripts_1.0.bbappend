FILESEXTRAPATHS_prepend_nokia900 := "${THISDIR}/${PN}:"

RDEPENDS_${PN}_append_nokia900 = " phonet-utils"

do_install_append_nokia900() {
    install -m 0755 ${WORKDIR}/nokia-n900-cmt-gpio.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/nokia-n900-cmt-gpio.sh ${D}${sysconfdir}/rcS.d/S40nokia-n900-cmt-gpio.sh
}

SRC_URI_append_nokia900 = " file://nokia-n900-cmt-gpio.sh"
PACKAGE_ARCH_nokia900 = "${MACHINE_ARCH}"
