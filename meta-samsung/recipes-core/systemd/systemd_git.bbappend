PRINC := "${@int(PRINC) + 3}"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_crespo = " \
  file://logind.conf \
"

do_install_append() {
        if [ "${MACHINE}" = "crespo" ]; then
                install -d ${D}${sysconfdir}
                install -m 0655 ${WORKDIR}/logind.conf ${D}${sysconfdir}/systemd/
        fi
}
