DESCRIPTION = "Utility programs for machine specific support"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = " \
    file://tenderloin-halium/bcattach/COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e \
    file://tenderloin-halium/bcattach/COPYING.LIB;md5=fb504b67c50331fc78734fed90fb0e09 \
"

PV = "0.1.0+git"
SRCREV = "7be1f8ab249229cc23d2566e136ceda0603303f8"

COMPATIBLE_MACHINE = "tenderloin-halium"
PACKAGE_ARCH:tenderloin-halium = "${MACHINE_ARCH}"

inherit cmake

SRC_URI = "git://github.com/webOS-ports/utilities.git;protocol=https;branch=master"

do_install:append:tenderloin-halium() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/tenderloin-halium/bcattach/files/systemd/tenderloin-bluetooth.service ${D}${systemd_unitdir}/system
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${S}/tenderloin-halium/bcattach/files/udev/tenderloin-bluetooth.rules ${D}${sysconfdir}/udev/rules.d
}

FILES:${PN}:tenderloin-halium += "${base_libdir} ${sbindir} ${sysconfdir}/udev/rules.d"
