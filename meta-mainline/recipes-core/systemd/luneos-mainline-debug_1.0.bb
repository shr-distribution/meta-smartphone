# Copyright (c) 2024-2025 LuneOS Project
# SPDX-License-Identifier: MIT

SUMMARY = "Debug services for mainline kernel development"
DESCRIPTION = "Systemd services and configuration to aid debugging during \
mainline Linux kernel bring-up. Includes USB gadget network setup, telnet \
server for early debug access, persistent logging, and USB watchdog. \
This package should only be included in development images."
HOMEPAGE = "https://webos-ports.org"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit systemd

SRC_URI = " \
    file://luneos-debug-usbnet.service \
    file://luneos-debug-telnet.service \
    file://luneos-debug-persistent-log.service \
    file://luneos-debug-usb-watchdog.service \
    file://journald-debug.conf \
"

S = "${WORKDIR}"

do_install() {
    # Install systemd services
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/luneos-debug-usbnet.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/luneos-debug-telnet.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/luneos-debug-persistent-log.service ${D}${systemd_system_unitdir}/
    install -m 0644 ${WORKDIR}/luneos-debug-usb-watchdog.service ${D}${systemd_system_unitdir}/

    # Install journald debug config
    install -d ${D}${sysconfdir}/systemd/journald.conf.d
    install -m 0644 ${WORKDIR}/journald-debug.conf ${D}${sysconfdir}/systemd/journald.conf.d/debug.conf
}

SYSTEMD_SERVICE:${PN} = " \
    luneos-debug-usbnet.service \
    luneos-debug-telnet.service \
    luneos-debug-persistent-log.service \
    luneos-debug-usb-watchdog.service \
"

FILES:${PN} = " \
    ${systemd_system_unitdir} \
    ${sysconfdir}/systemd/journald.conf.d \
"

# Runtime dependencies
RDEPENDS:${PN} = " \
    busybox \
    iproute2 \
"
