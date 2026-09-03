# Copyright (c) 2026 Herman van Hazendonk <github.com@herrie.org>

SUMMARY = "Restart MediaTek's RIL when it crashes"
DESCRIPTION = "mtkfusionrild is declared oneshot in the vendor's mtkrild.rc, so \
Android's init does not respawn it and a crash takes telephony out for the rest \
of the boot. The vendor partition is read-only, so ask init for an explicit \
start instead - which oneshot does not prevent."
SECTION = "devices"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# Machine-specific: the fault, the service name and the rc that declares it
# oneshot are all this device's. Another MTK port wanting the same thing should
# confirm its own rc first rather than inherit ours by accident.
COMPATIBLE_MACHINE = "mindphone"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
    file://mtk-ril-watchdog \
    file://mtk-ril-watchdog.service \
    file://mtk-ril-watchdog.timer \
"

S = "${UNPACKDIR}"

inherit systemd

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${UNPACKDIR}/mtk-ril-watchdog ${D}${sbindir}/mtk-ril-watchdog

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-ril-watchdog.service ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/mtk-ril-watchdog.timer ${D}${systemd_system_unitdir}
}

# The timer is what gets enabled; it pulls the service in when it fires.
SYSTEMD_SERVICE:${PN} = "mtk-ril-watchdog.timer"

# systemd.bbclass only packages the units named in SYSTEMD_SERVICE (plus an
# Also= socket), so the service the timer starts has to be listed by hand.
# It stays out of SYSTEMD_SERVICE deliberately: it carries no [Install]
# section because nothing should enable it directly - the timer is what pulls
# it in.
FILES:${PN} += "${systemd_system_unitdir}/mtk-ril-watchdog.service"
