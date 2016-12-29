FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

do_install_append_crespo() {
    # install additional mount directory for modem related data
    install -m 0755 -d ${D}/efs
}

do_install_append_tuna() {
    # install additional mount directory for root of original android system
    install -m 0755 -d ${D}/system

    # install additional mount directory for modem related data
    install -m 0755 -d ${D}/factory

    # required by rild
    install -m 0755 -d ${D}/data/radio
    install -m 0755 -d ${D}/data/radio/log
}
