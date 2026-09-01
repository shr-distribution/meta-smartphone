FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The machine appends below add nothing but local files, so nothing lands in
# the default S = "${UNPACKDIR}/${BP}" and do_qa_unpack warns about it.
# Scoped per machine rather than set outright: this bbappend is parsed for
# every build, and a bare S here would also apply to MACHINEs that get no
# SRC_URI from this layer at all.
S:mido-halium = "${UNPACKDIR}"
S:oxygen = "${UNPACKDIR}"
S:sagit = "${UNPACKDIR}"
S:tissot-halium = "${UNPACKDIR}"

SRC_URI:append:mido-halium = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
"

SRC_URI:append:oxygen = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
"

SRC_URI:append:sagit = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
"

SRC_URI:append:tissot-halium = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
"

do_install:append:mido-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

do_install:append:oxygen() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

do_install:append:sagit() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

do_install:append:tissot-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:mido-halium = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
"

SYSTEMD_SERVICE:${PN}:oxygen = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
"

SYSTEMD_SERVICE:${PN}:sagit = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
"

SYSTEMD_SERVICE:${PN}:tissot-halium = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
"
