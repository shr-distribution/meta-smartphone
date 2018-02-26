FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append_angler() {
    # install additional mount directory for root of original android system
    install -m 0755 -d ${D}/system
    install -d ${D}/firmware
    install -d ${D}/persist
}
