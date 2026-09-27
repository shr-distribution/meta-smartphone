SUMMARY = "USB debug channel (rndis + adb) for the FuriPhone FLX1s"
DESCRIPTION = "Builds a single USB gadget carrying both rndis and adb, in the \
order FunctionFS requires, and falls back to rndis alone if the combined gadget \
will not bind. Also addresses and raises the gadget netdev, which the kernel \
leaves down. Exists because the FLX1s has no exposed UART and, until Wi-Fi \
worked, no other way in at all."
SECTION = "base"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# Deliberately radon-only, not every Halium machine.
#
# The ordering this encodes is generic - any Halium device wanting rndis and adb
# on one gadget needs it - but turning a USB gadget on by default everywhere
# changes behaviour on devices that currently work and that I cannot test. The
# sensible path to generalising it is to move this to meta-android with an
# opt-in deviceinfo flag, once it has proven itself here.
COMPATIBLE_MACHINE = "^radon$"

PV = "1.0"

# Nothing but local files, so nothing lands in the default S = ${UNPACKDIR}/${BP}
# and do_unpack warns about it.
S = "${UNPACKDIR}"

SRC_URI = " \
    file://luneos-usb-setup.sh \
    file://usb-ip.sh \
    file://99-radon-usbnet.rules \
    file://radon-usb-debug.service \
    file://adbd-no-gadget-setup.conf \
    file://adbd-early-start.conf \
"

inherit systemd

do_install() {
    install -D -m 0755 ${UNPACKDIR}/luneos-usb-setup.sh ${D}/usr/local/bin/luneos-usb-setup.sh
    install -D -m 0755 ${UNPACKDIR}/usb-ip.sh           ${D}/usr/local/bin/usb-ip.sh
    install -D -m 0644 ${UNPACKDIR}/99-radon-usbnet.rules ${D}${sysconfdir}/udev/rules.d/99-radon-usbnet.rules

    install -D -m 0644 ${UNPACKDIR}/radon-usb-debug.service \
        ${D}${systemd_system_unitdir}/radon-usb-debug.service

    # adbd must not rebuild the gadget underneath us - see the drop-in itself.
    install -D -m 0644 ${UNPACKDIR}/adbd-no-gadget-setup.conf \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/10-no-gadget-setup.conf

    # ...and it has to be startable in the early window that waits for it.
    install -D -m 0644 ${UNPACKDIR}/adbd-early-start.conf \
        ${D}${systemd_system_unitdir}/android-tools-adbd.service.d/20-early-start.conf

    # adbd's own unit has ConditionPathExists=/etc/usb-debugging-enabled.
    install -d ${D}${sysconfdir}
    touch ${D}${sysconfdir}/usb-debugging-enabled
}

# Only radon-usb-debug.service is enabled. android-tools-adbd is deliberately
# NOT enabled: luneos-usb-setup.sh starts it at the one point in the sequence
# where FunctionFS will accept it, and letting systemd start it independently
# reintroduces the race this recipe exists to avoid.
SYSTEMD_SERVICE:${PN} = "radon-usb-debug.service"

RDEPENDS:${PN} = "android-tools-adbd android-tools-conf"

FILES:${PN} += " \
    /usr/local/bin/luneos-usb-setup.sh \
    /usr/local/bin/usb-ip.sh \
    ${sysconfdir}/udev/rules.d/99-radon-usbnet.rules \
    ${sysconfdir}/usb-debugging-enabled \
    ${systemd_system_unitdir}/android-tools-adbd.service.d/10-no-gadget-setup.conf \
    ${systemd_system_unitdir}/android-tools-adbd.service.d/20-early-start.conf \
"
