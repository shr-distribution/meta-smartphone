FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_crespo = " \
  file://rndissetup.sh \
  file://rndissetup.service \
  file://disablefbcon.sh \
  file://disablefbcon.service \
"

SRC_URI_append_tuna = " \
  file://rndissetup.sh \
  file://rndissetup.service \
"

do_install_append() {
        if [ "${MACHINE}" = "crespo" -o "${MACHINE}" = "tuna" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${bindir}
        fi
        if [ "${MACHINE}" = "crespo" ]; then
                install -m 0755 ${WORKDIR}/disablefbcon.sh ${D}${bindir}
        fi
}

SYSTEMD_SERVICE_crespo = "rndissetup.service disablefbcon.service"
SYSTEMD_SERVICE_tuna = "rndissetup.service"


PRINC := "${@int(PRINC) + 7}"
