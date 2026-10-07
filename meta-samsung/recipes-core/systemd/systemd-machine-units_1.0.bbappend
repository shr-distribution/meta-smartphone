FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Nothing but local files, so nothing lands in the default
# S = "${UNPACKDIR}/${BP}" and do_qa_unpack warns about it. Scoped per machine,
# as in meta-xiaomi: this bbappend is parsed for every build.
S:sm-t220 = "${UNPACKDIR}"

SRC_URI:append:sm-t220 = " \
    file://mtk-audio-param-fixup.service \
    file://mtk-audio-param-fixup.sh \
"

do_install:append:sm-t220() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/mtk-audio-param-fixup.service ${D}${systemd_unitdir}/system

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/mtk-audio-param-fixup.sh ${D}${bindir}
}

SYSTEMD_SERVICE:${PN}:append:sm-t220 = " mtk-audio-param-fixup.service"
