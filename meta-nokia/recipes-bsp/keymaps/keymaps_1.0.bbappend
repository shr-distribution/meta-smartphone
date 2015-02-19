FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_nokia900 = " file://keymap-2.6.map"

do_install_append_nokia900 () {
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/keymap-*.map     ${D}${sysconfdir}
}
