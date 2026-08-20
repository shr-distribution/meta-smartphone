DESCRIPTION = "Derive device configuration at runtime so one rootfs boots on any device"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"
SECTION = "base"

# Deliberately not MACHINE_ARCH: the whole point is that this package is the
# same on every device. The per-device data it reads is content, not build
# configuration, and all adaptations ship together - the entire set is a few
# hundred KB.
PACKAGE_ARCH = "${TUNE_PKGARCH}"

# udevadm for input capability queries, and the container's getprop.
RDEPENDS:${PN} = "udev"

inherit systemd

SRC_URI = " \
    file://luneos-device-config \
    file://luneos-device-config.service \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/luneos-device-config ${D}${bindir}

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/luneos-device-config.service ${D}${systemd_unitdir}/system

    install -d ${D}${datadir}/luneos/adaptations
    cp -r ${THISDIR}/adaptations/* ${D}${datadir}/luneos/adaptations/
}

FILES:${PN} += "${datadir}/luneos/adaptations"

SYSTEMD_SERVICE:${PN} = "luneos-device-config.service"
