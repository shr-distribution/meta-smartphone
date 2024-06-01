FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:tenderloin = " \
    file://tenderloin-ath6kl-module.service \
    file://tenderloin-swap.service \
"

# RDEPENDS:${PN}:tenderloin += "tenderloin-bluetooth-utilities"

do_install:append:tenderloin() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/tenderloin-ath6kl-module.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/tenderloin-swap.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN}:tenderloin = "tenderloin-ath6kl-module.service tenderloin-swap.service"


SRC_URI:append:tenderloin-halium = " \
    file://tenderloin-ath6kl-module.service \
    file://tenderloin-swap.service \
"

RDEPENDS:${PN}:tenderloin-halium += "tenderloin-bluetooth-utilities"

do_install:append:tenderloin-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/tenderloin-ath6kl-module.service ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/tenderloin-swap.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE:${PN}:tenderloin-halium = "tenderloin-ath6kl-module.service tenderloin-swap.service"
