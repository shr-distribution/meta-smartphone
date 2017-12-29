FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_athene = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install_append_athene() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE_${PN}_athene = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"
