FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append_mako() {
    install -m 0755 -d ${D}/firmware
}
