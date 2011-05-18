THISDIR := "${@os.path.dirname(bb.data.getVar('FILE', d, True))}"
FILESPATH =. "${@base_set_filespath(["${THISDIR}/${PN}"], d)}:"

SRC_URI_append_nokia900 = " \
       file://10-cmt_speech.rules \
       file://70-persistent-net.rules \
       file://udev-rules-nokia-n900-hacks.rules \
       file://udev-rules-nokia-n900-snd.rules \
       file://nokia-n900-mac-hack.sh \
"

do_install_append_nokia900() {
        install -m 0644 ${WORKDIR}/10-cmt_speech.rules ${D}${sysconfdir}/udev/rules.d/10-cmt_speech.rules
        install -m 0644 ${WORKDIR}/70-persistent-net.rules ${D}${sysconfdir}/udev/rules.d/70-persistent-net.rules
        install -m 0644 ${WORKDIR}/udev-rules-nokia-n900-hacks.rules ${D}${sysconfdir}/udev/rules.d/udev-rules-nokia-n900-hacks.rules
        install -m 0644 ${WORKDIR}/udev-rules-nokia-n900-snd.rules ${D}${sysconfdir}/udev/rules.d/udev-rules-nokia-n900-snd.rules
        install -m 0755 ${WORKDIR}/nokia-n900-mac-hack.sh ${D}${sysconfdir}/udev/scripts/nokia-n900-mac-hack.sh
}

PACKAGE_ARCH_nokia900 = "nokia900"

