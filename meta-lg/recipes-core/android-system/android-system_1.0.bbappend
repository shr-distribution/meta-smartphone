FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_mako = " \
    file://50-firmware \
    file://51-setup-wifi \
"

do_install_append_mako() {
    install -m 0755 ${WORKDIR}/50-firmware ${D}${localstatedir}/lib/lxc/android/pre-start.d/
    install -m 0755 ${WORKDIR}/51-setup-wifi ${D}${localstatedir}/lib/lxc/android/pre-start.d/
}
