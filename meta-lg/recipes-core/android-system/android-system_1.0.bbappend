FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_mako = " file://50-firmware"

do_install_append_mako() {
    # We need some extra script to bind mount the firmware partition
    install -m 0755 ${WORKDIR}/50-firmware ${D}${localstatedir}/lib/lxc/android/pre-start.d/
}
