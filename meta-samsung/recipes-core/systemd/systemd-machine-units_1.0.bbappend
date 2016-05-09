FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

RNDISSETUP_URI = "${@bb.utils.contains('DISTRO_FEATURES', 'adb', '', \
                                   'file://rndissetup.sh file://rndissetup.service', d)}"

SRC_URI_append_crespo = " \
  ${RNDISSETUP_URI} \
  file://disablefbcon.sh \
  file://disablefbcon.service \
  file://logind.conf \
"

SRC_URI_append_tuna = " \
  ${RNDISSETUP_URI} \
  file://pvrinit.service \
"

SRC_URI_append_i9300 = " \
  ${RNDISSETUP_URI} \
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

do_install_append_i9300() {
    install_common
}


RNDISSETUP_SERVICE = "${@bb.utils.contains('DISTRO_FEATURES', 'adb', '', 'rndissetup.service', d)}"

SYSTEMD_SERVICE_${PN}_crespo = "${RNDISSETUP_SERVICE} disablefbcon.service"
SYSTEMD_SERVICE_${PN}_tuna   = "${RNDISSETUP_SERVICE} pvrinit.service"
SYSTEMD_SERVICE_${PN}_i9300  = "${RNDISSETUP_SERVICE}"
