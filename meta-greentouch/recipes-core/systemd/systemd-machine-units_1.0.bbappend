FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# MediaTek combo bring-up (module load, wifi power-on, BT) now comes from the
# generic mtk-connectivity recipe in meta-android, not from a per-machine
# wifi-module-load.service. The old hciattach/dev-ttyHS99 units were Qualcomm
# UART-BT copy-paste and never applied to this MTK device (BT is stpbt/vhci via
# bluebinder). Only the wifi MAC persister is machine-specific here.
SRC_URI:append:mindphone = " \
    file://wifi-macaddr-persister.service \
    file://persist-wifi-mac-addr.sh \
"

do_install:append:mindphone() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:mindphone = " \
    wifi-macaddr-persister.service \
"
