FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

LICENSES += "GPL-3.0"
LIC_FILES_CHKSUM += "file://${WORKDIR}/65-android.rules;beginline=4;endline=17;md5=534bfbe4a9959287b397927ee891ea85"

SRC_URI += "file://65-android.rules"

do_install_append() {
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${WORKDIR}/65-android.rules ${D}${sysconfdir}/udev/rules.d/
}
