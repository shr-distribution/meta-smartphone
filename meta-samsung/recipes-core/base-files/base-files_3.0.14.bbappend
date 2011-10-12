FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
PRINC = "1"

do_install_append_crespo() {
    # install additional mount directory for modem related data
    install -m 0755 -d ${D}/efs
}
