FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_tenderloin = " \
    file://tenderloin-ath6kl-module.service \
    file://tenderloin-swap.service \
"

do_install_append_tenderloin() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/tenderloin-ath6kl-module.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/tenderloin-swap.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE_${PN}_tenderloin = "tenderloin-ath6kl-module.service tenderloin-swap.service"
