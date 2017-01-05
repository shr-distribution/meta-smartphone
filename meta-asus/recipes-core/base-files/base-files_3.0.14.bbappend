FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append_grouper() {
    # install additional mount directory for root of original android system
    install -m 0755 -d ${D}/system
}
