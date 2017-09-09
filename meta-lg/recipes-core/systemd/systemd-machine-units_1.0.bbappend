FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

COMPATIBLE_MACHINE_mako = "mako"
COMPATIBLE_MACHINE_hammerhead = "hammerhead"

SRC_URI_append = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hcismd.service \
    file://hci-smd-enable.sh \
"

do_install_append() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/hcismd.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${WORKDIR}/hci-smd-enable.sh ${D}${bindir}
}

SYSTEMD_SERVICE_${PN} = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hcismd.service \
"

