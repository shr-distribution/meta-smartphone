FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_om-gta02 = " \
  file://g_ether.sh \
  file://g_ether.service \
"

do_install_append() {
        if [ "${MACHINE}" = "om-gta02" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/g_ether.sh ${D}${bindir}
        fi
}

SYSTEMD_SERVICE_om-gta02 = "g_ether.service"

#Add GPS for gta04
RDEPENDS_${PN}_om-gta04 = "gta04-gps-handler-systemd"

PRINC := "${@int(PRINC) + 3}"
