# Ship the Minimal Phone MP01 Tier 1 adaptation. Installed unconditionally like
# every other adaptation - it only takes effect when the running device's
# codename matches - so no COMPATIBLE_MACHINE guard. Named flatly rather than as
# an adaptations/ tree so it does not shadow the base recipe's own adaptations
# directory in the file:// search path.
#
# The directory name has to equal what luneos-device-config derives as the
# codename, which it reads from ro.product.vendor.device first.
#
# CONFIRMED against the stock firmware's own vendor partition
# (Z10_20251226_user_smr6.zip, super.img -> vendor_a -> /build.prop):
#   ro.product.vendor.device=MP01
#   ro.product.vendor.name=MP01
# so "MP01" is the authoritative spelling, not the "Z10" that the factory
# image filename, the scatter's project field and the panel driver all use -
# Z10 is the ODM's internal project name and never reaches this property.
# The lowercase symlink is cheap insurance.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://MP01-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/MP01
    install -m 0644 ${UNPACKDIR}/MP01-deviceinfo \
        ${D}${datadir}/luneos/adaptations/MP01/deviceinfo
    ln -sfn MP01 ${D}${datadir}/luneos/adaptations/mp01
}
