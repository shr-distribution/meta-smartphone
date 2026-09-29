DESCRIPTION = "Firmware bits for the Samsung Galaxy A3 (2015) that are not on the firmware partitions"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

PACKAGE_ARCH = "${MACHINE_ARCH}"

COMPATIBLE_MACHINE = "^a3-2015$"

PV = "1.0"
PR = "r0"

# No proprietary blob is redistributed here: the one file this recipe is about
# is read off the device's own stock system partition at first boot. See
# samsung-a3-wcnss-nv.sh for why that is the only sane source for it.
SRC_URI = " \
    file://samsung-a3-wcnss-nv.sh \
    file://samsung-a3-wcnss-nv.service \
"

S = "${UNPACKDIR}"

inherit systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "samsung-a3-wcnss-nv.service"

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 ${UNPACKDIR}/samsung-a3-wcnss-nv.sh ${D}${sbindir}/samsung-a3-wcnss-nv.sh

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${UNPACKDIR}/samsung-a3-wcnss-nv.service ${D}${systemd_unitdir}/system/

    # The directory the script writes into, so the kernel's firmware loader has
    # a path to find rather than one to create.
    install -d ${D}${nonarch_base_libdir}/firmware/wlan/prima
}

FILES:${PN} += "${nonarch_base_libdir}/firmware"

RDEPENDS:${PN} = "util-linux-mount"
