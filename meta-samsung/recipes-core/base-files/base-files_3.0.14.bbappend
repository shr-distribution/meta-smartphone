FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

PRINC := "${@int(PRINC) + 4}"

do_install_append_crespo() {
    # install additional mount directory for modem related data
    install -m 0755 -d ${D}/efs
}

do_install_append_tuna() {
    # install additional mount directory for modem related data
    install -m 0755 -d ${D}/factory
}
