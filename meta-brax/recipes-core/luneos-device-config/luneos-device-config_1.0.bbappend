# Ship the BraX3 Tier 1 adaptation. Installed unconditionally like every other
# adaptation - it only takes effect when the running device's codename matches -
# so no COMPATIBLE_MACHINE guard.
#
# luneos-device-config reads the codename from ro.product.vendor.device first,
# verbatim. Measured on a BraX3 running iodeOS 6.3 (LineageOS 22, 26 May 2025):
# "BraX3", capitals included, although ro.lineage.device is "brax3" and BaseOS
# sets PRODUCT_DEVICE := brax3 (baseos_brax3.mk). The same phone on iodeOS 7.2
# and 7.3 (Android 16) reports "brax3". Both spellings are in use, so the
# directory is named BraX3 with a lowercase symlink - the same arrangement as
# the Q25's.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://brax3-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/BraX3
    install -m 0644 ${UNPACKDIR}/brax3-deviceinfo \
        ${D}${datadir}/luneos/adaptations/BraX3/deviceinfo
    ln -sfn BraX3 ${D}${datadir}/luneos/adaptations/brax3
}
