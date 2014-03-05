FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

# NOTE: As we're using android usb composite driver we need to enable rndis support manually on startup.
install_common() {
    install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S20rndissetup.sh
}

do_install_append_tuna() {
    install_common
}

do_install_append_crespo() {
    install_common
}

SRC_URI_append_crespo = " \
    file://rndissetup.sh \
"

SRC_URI_append_tuna = " \
    file://rndissetup.sh \
"

PACKAGE_ARCH_crespo = "${MACHINE_ARCH}"
PACKAGE_ARCH_tuna = "${MACHINE_ARCH}"
