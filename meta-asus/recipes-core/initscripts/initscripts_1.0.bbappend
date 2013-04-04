FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC := "${@int(PRINC) + 1}"

# NOTE: As we're using android usb composite driver we need to enable rndis support manually on startup.
do_install_append() {
    if [ "${MACHINE}" = "grouper" ]; then
        install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
        ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S20rndissetup.sh
    fi
}

SRC_URI_append_grouper = " \
    file://rndissetup.sh \
"

PACKAGE_ARCH_grouper = "${MACHINE_ARCH}"
