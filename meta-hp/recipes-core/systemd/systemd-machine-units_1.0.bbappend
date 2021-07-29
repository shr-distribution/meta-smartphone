FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tenderloin = " \
    file://tenderloin-ath6kl-module.service \
    file://tenderloin-swap.service \
"

RDEPENDS:${PN}:tenderloin += "tenderloin-bluetooth-utilities"

do_install:append:tenderloin() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/tenderloin-ath6kl-module.service ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/tenderloin-swap.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN}:tenderloin = "tenderloin-ath6kl-module.service tenderloin-swap.service"
