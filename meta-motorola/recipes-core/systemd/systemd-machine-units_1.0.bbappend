FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The machine append below adds nothing but local files, so nothing lands in
# the default S = "${UNPACKDIR}/${BP}" and do_qa_unpack warns about it.
# Scoped per machine rather than set outright: this bbappend is parsed for
# every build, and a bare S here would also apply to MACHINEs that get no
# SRC_URI from this layer at all.
S:athene = "${UNPACKDIR}"

SRC_URI:append:athene = " \
    file://wifi-macaddr-persister.service \
    file://wifi-module-load.service \
    file://persist-wifi-mac-addr.sh \
    file://hciattach.service \
    file://hciattach.sh \
    file://dev-ttyHS99.device \
"

do_install:append:athene() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-macaddr-persister.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/hciattach.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/dev-ttyHS99.device ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/wifi-module-load.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/persist-wifi-mac-addr.sh ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/hciattach.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:athene = " \
    wifi-macaddr-persister.service \
    wifi-module-load.service \
    hciattach.service \
    dev-ttyHS99.device \
"
