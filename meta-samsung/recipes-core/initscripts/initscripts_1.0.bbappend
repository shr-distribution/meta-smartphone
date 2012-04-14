FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 2}"

# NOTE: As we're using android usb composite driver we need to enable rndis support manually on startup.
do_install_append() {
    if [ "${MACHINE}" = "crespo" ]; then
        install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
        ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S03rndissetup.sh
    fi
}

SRC_URI_append_crespo = " \
    file://rndissetup.sh \
"
PACKAGE_ARCH_crespo = "${MACHINE_ARCH}"
