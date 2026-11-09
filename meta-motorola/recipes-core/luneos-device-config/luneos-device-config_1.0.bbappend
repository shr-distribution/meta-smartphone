# Ship the Motorola moto g06 Tier 1 adaptation. Installed unconditionally like
# every other adaptation - it only takes effect when the running device's
# codename matches - so no COMPATIBLE_MACHINE guard.
#
# The directory name has to equal what luneos-device-config derives as the
# codename, which it reads from ro.product.vendor.device first. The stock
# vendor build.prop (VVOB35.78-188) has ro.product.vendor.device=lagos.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://lagos-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/lagos
    install -m 0644 ${UNPACKDIR}/lagos-deviceinfo \
        ${D}${datadir}/luneos/adaptations/lagos/deviceinfo
}
