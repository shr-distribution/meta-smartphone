FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Nothing but local files, so nothing lands in the default S = "${UNPACKDIR}/${BP}" and do_qa_unpack
# warns about it. Scoped per machine: this bbappend is parsed for every build.
S:flo-halium = "${UNPACKDIR}"

# Nexus 7 (2013) Wi-Fi: Wi-Fi and Bluetooth are the two halves of the WCNSS (Riva) core, both driven from
# the host once the container has booted it. See the units for what each does. The names carry the machine,
# because another layer's bbappend (meta-xiaomi) puts its directory first in the search path for every
# machine, and its own hciattach.service won over sm-t520's when the names were the same.
SRC_URI:append:flo-halium = " \
    file://flo-prima-wlan.service \
    file://flo-hci-smd.service \
    file://60-flo-loop-noprobe.rules \
"

do_install:append:flo-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/flo-prima-wlan.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/flo-hci-smd.service ${D}${systemd_unitdir}/system
    install -d ${D}${nonarch_base_libdir}/udev/rules.d
    install -m 0644 ${UNPACKDIR}/60-flo-loop-noprobe.rules ${D}${nonarch_base_libdir}/udev/rules.d/
}

FILES:${PN}:append:flo-halium = " ${nonarch_base_libdir}/udev/rules.d/60-flo-loop-noprobe.rules"
SYSTEMD_SERVICE:${PN}:append:flo-halium = " flo-prima-wlan.service flo-hci-smd.service"
