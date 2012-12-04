FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 6}"

# NOTE: As we're using android usb composite driver we need to enable rndis support manually on startup.
do_install_append() {
    if [ "${MACHINE}" = "crespo" -o "${MACHINE}" = "tuna" ]; then
        install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
        ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S20rndissetup.sh
    fi
}

SRC_URI_append_crespo = " \
    file://rndissetup.sh \
"

SRC_URI_append_tuna = " \
    file://rndissetup.sh \
"

PACKAGE_ARCH_crespo = "${MACHINE_ARCH}"
PACKAGE_ARCH_tuna = "${MACHINE_ARCH}"
