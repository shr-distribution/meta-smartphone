FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_tuna = " \
    file://50-maguro-fix-pvrpath \
"

do_install_append_tuna() {
    install -d ${D}${localstatedir}/lib/lxc/android/pre-start.d
    install -m 0755 ${WORKDIR}/50-maguro-fix-pvrpath ${D}${localstatedir}/lib/lxc/android/pre-start.d/
}
