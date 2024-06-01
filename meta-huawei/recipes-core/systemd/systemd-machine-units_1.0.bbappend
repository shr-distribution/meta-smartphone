FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:angler = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:angler() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:angler = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"
