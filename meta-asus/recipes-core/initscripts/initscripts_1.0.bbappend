FILESEXTRAPATHS_prepend_grouper := "${THISDIR}/${PN}:"

# NOTE: As we're using android usb composite driver we need to enable rndis support manually on startup.
do_install_append_grouper() {
    install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${sysconfdir}/init.d
    ln -sf ../init.d/rndissetup.sh ${D}${sysconfdir}/rcS.d/S20rndissetup.sh
}

SRC_URI_append_grouper = " file://rndissetup.sh"
PACKAGE_ARCH_grouper = "${MACHINE_ARCH}"
