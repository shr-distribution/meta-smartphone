DESCRIPTION = "Utility programs for machine specific support"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = " \
    file://tenderloin/bcattach/COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e \
    file://tenderloin/bcattach/COPYING.LIB;md5=fb504b67c50331fc78734fed90fb0e09 \
"

PV = "0.1.0+git${SRCPV}"
SRCREV = "cb092c244ff19a2fe27df65b920245c325654df1"

COMPATIBLE_MACHINE = "tenderloin"
PACKAGE_ARCH_tenderloin = "${MACHINE_ARCH}"

inherit cmake

SRC_URI = "git://github.com/webOS-ports/utilities.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

do_install_append_tenderloin() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/tenderloin/bcattach/files/systemd/tenderloin-bluetooth.service ${D}${systemd_unitdir}/system
    install -d ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${S}/tenderloin/bcattach/files/udev/tenderloin-bluetooth.rules ${D}${sysconfdir}/udev/rules.d
}

FILES_${PN}_tenderloin += "${base_libdir} ${sbindir} ${sysconfdir}/udev/rules.d"
