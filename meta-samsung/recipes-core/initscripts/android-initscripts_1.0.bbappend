FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}"
PRINC := "${@int(PRINC) + 1}"

SRC_URI_append_tuna = " file://pvrinit.conf"

do_install_append_tuna() {
    install -d ${D}${sysconfdir}/event.d
    install -m 0644 ${WORKDIR}/pvrinit.conf ${D}${sysconfdir}/event.d/pvrinit
}
