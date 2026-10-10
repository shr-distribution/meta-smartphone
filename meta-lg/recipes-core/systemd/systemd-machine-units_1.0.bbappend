FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The machine appends below add nothing but local files, so nothing lands in
# the default S = "${UNPACKDIR}/${BP}" and do_qa_unpack warns about it.
# Scoped per machine rather than set outright: this bbappend is parsed for
# every build, and a bare S here would also apply to MACHINEs that get no
# SRC_URI from this layer at all.
S:mako = "${UNPACKDIR}"
S:hammerhead = "${UNPACKDIR}"
S:hammerhead-halium = "${UNPACKDIR}"
S:mako-halium = "${UNPACKDIR}"

SRC_URI:append:mako = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:mako() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:mako = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"


SRC_URI:append:hammerhead = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:hammerhead() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:hammerhead = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"


SRC_URI:append:hammerhead-halium = " \
    file://android-host-apexes \
    file://ofono-lte-cap.service \
    file://ofono-lte-cap.sh \
    file://ofono-settle.conf \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:hammerhead-halium() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/android-host-apexes ${D}${sysconfdir}/android-host-apexes

    install -d ${D}${systemd_unitdir}/system/ofono.service.d
    install -m 0644 ${UNPACKDIR}/ofono-settle.conf ${D}${systemd_unitdir}/system/ofono.service.d/ofono-settle.conf

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/ofono-lte-cap.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/ofono-lte-cap.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:hammerhead-halium = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    ofono-lte-cap.service \
    dev-ttyHS99.device \
"


SRC_URI:append:mako-halium = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:mako-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:mako-halium = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"


# Nexus 5X: see bullhead-halium/sensorfwd-restart.conf and wam-maps-capture.service (found
# through the machine override).
S:bullhead-halium = "${UNPACKDIR}"

SRC_URI:append:bullhead-halium = " file://sensorfwd-restart.conf file://wam-maps-capture.service file://wam-maps-capture.sh"

do_install:append:bullhead-halium() {
    install -d ${D}${systemd_unitdir}/system/sensorfwd.service.d
    install -m 0644 ${UNPACKDIR}/sensorfwd-restart.conf \
        ${D}${systemd_unitdir}/system/sensorfwd.service.d/sensorfwd-restart.conf
    install -m 0644 ${UNPACKDIR}/wam-maps-capture.service ${D}${systemd_unitdir}/system
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/wam-maps-capture.sh ${D}${bindir}
}

FILES:${PN}:append:bullhead-halium = " ${systemd_unitdir}/system/sensorfwd.service.d ${systemd_unitdir}/system/wam-maps-capture.service ${bindir}/wam-maps-capture.sh"
SYSTEMD_SERVICE:${PN}:append:bullhead-halium = " wam-maps-capture.service"
