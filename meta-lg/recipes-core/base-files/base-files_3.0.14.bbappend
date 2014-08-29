FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append_mako() {
    install -d ${D}/firmware
    install -d ${D}/persist
}
