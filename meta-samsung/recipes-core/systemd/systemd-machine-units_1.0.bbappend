FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_crespo = " \
  ${@base_contains('DISTRO_FEATURES', 'adb', '', 'file://rndissetup.sh', d)} \
  ${@base_contains('DISTRO_FEATURES', 'adb', '', 'file://rndissetup.service', d)} \
  file://disablefbcon.sh \
  file://disablefbcon.service \
  file://logind.conf \
"

SRC_URI_append_tuna = " \
  ${@base_contains('DISTRO_FEATURES', 'adb', '', 'file://rndissetup.sh', d)} \
  ${@base_contains('DISTRO_FEATURES', 'adb', '', 'file://rndissetup.service', d)} \
  file://pvrinit.service \
"

install_common() {
    if [ -e ${WORKDIR}/rndissetup.sh ] && [ -e ${WORKDIR}/rndissetup.service ] ; then
        install -d ${D}${bindir}
        install -m 0755 ${WORKDIR}/rndissetup.sh ${D}${bindir}
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/rndissetup.service ${D}${systemd_unitdir}/system
    fi
}

do_install_append_tuna() {
    install_common
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/pvrinit.service ${D}${systemd_unitdir}/system
}

do_install_append_crespo() {
    install_common
    install -m 0755 ${WORKDIR}/disablefbcon.sh ${D}${bindir}
    install -d ${D}${sysconfdir}/systemd
    install -m 0655 ${WORKDIR}/logind.conf ${D}${sysconfdir}/systemd/
}

SYSTEMD_SERVICE_${PN}_crespo = "rndissetup.service disablefbcon.service"
SYSTEMD_SERVICE_${PN}_tuna = "${@base_contains('DISTRO_FEATURES', 'adb', '', 'rndissetup.service', d)} pvrinit.service"
