FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_tenderloin = " \
    file://load-ath6kl-module.service \
"

do_install_append_tenderloin() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/load-ath6kl-module.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE_${PN}_tenderloin = "load-ath6kl-module.service"
