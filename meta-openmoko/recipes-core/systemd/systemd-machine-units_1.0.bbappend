FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_om-gta02 = " \
  file://g_ether.sh \
  file://g_ether.service \
"
SRC_URI_append_om-gta04 = " \
  file://g_ether.sh \
  file://g_ether.service \
"

do_install_append() {
        if [ "${MACHINE}" = "om-gta02" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/g_ether.sh ${D}${bindir}
                install -d ${D}${systemd_unitdir}/system
                install -m 0644 ${WORKDIR}/g_ether.service ${D}${systemd_unitdir}/system
        fi
        if [ "${MACHINE}" = "om-gta04" ]; then
                install -d ${D}${bindir}
                install -m 0755 ${WORKDIR}/g_ether.sh ${D}${bindir}
                install -d ${D}${systemd_unitdir}/system
                install -m 0644 ${WORKDIR}/g_ether.service ${D}${systemd_unitdir}/system
        fi
}

SYSTEMD_SERVICE_${PN}_om-gta02 = "g_ether.service"
SYSTEMD_SERVICE_${PN}_om-gta04 = "g_ether.service"
