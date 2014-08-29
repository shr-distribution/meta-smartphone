FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_mako = " \
    file://wifi-macaddr-persister.service \
    file://persist-wifi-mac-addr.sh \
"

do_install_append_mako() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
}

SYSTEMD_SERVICE_${PN}_mako = "wifi-macaddr-persister.service"
