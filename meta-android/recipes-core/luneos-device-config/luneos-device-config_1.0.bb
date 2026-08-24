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

# The generators and adaptations live in the ${BPN} files directory and are
# listed here rather than copied out of ${THISDIR} at do_install time. A file
# bitbake does not know about does not contribute to the task signature, so
# editing a generator produced no rebuild at all and the previous one stayed in
# the image - silently, because nothing failed.
SRC_URI = " \
    file://luneos-device-config \
    file://luneos-device-config.service \
    file://generators \
    file://adaptations \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/luneos-device-config ${D}${bindir}

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/luneos-device-config.service ${D}${systemd_unitdir}/system

    install -d ${D}${datadir}/luneos/adaptations
    cp -r ${UNPACKDIR}/adaptations/* ${D}${datadir}/luneos/adaptations/

    install -d ${D}${libdir}/luneos-device-config/generators
    install -m 0755 ${UNPACKDIR}/generators/* ${D}${libdir}/luneos-device-config/generators/

    # Ship the configd override layer directory empty. /etc/configd/layers.json
    # already declares it at priority 900; shipping the mount point means the
    # runtime bind does not have to mkdir into the rootfs.
    install -d ${D}${sysconfdir}/configd/layers/overlay
}

FILES:${PN} += "${datadir}/luneos/adaptations ${libdir}/luneos-device-config ${sysconfdir}/configd/layers/overlay"

SYSTEMD_SERVICE:${PN} = "luneos-device-config.service"
