FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:sargo = " file://stubbed-services"

do_install:append:sargo() {
    install -m 0644 ${UNPACKDIR}/stubbed-services ${D}${localstatedir}/lib/lxc/android/stubbed-services
}
