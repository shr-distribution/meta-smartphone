# Ship the FuriPhone FLX1s Tier 1 adaptation. Installed unconditionally like
# every other adaptation - it only takes effect when the running device's
# codename matches - so no COMPATIBLE_MACHINE guard. Named flatly rather than
# as an adaptations/ tree so it does not shadow the base recipe's own
# adaptations directory in the file:// search path.
#
# The directory name has to equal what luneos-device-config derives as the
# codename, which it reads in this order:
#   ro.product.vendor.device -> ro.vendor.product.device -> ro.product.device
#   -> the part after the comma in /proc/device-tree/compatible
#
# CONFIRMED from FuriLabs' own shipping image (flx1s-furios-stable.tar.gz,
# super.img -> vendor/odm build.prop):
#
#   ro.product.vendor.device=radon      ro.product.odm.device=radon
#   ro.product.vendor.name=radon        ro.product.odm.name=radon
#   ro.product.vendor.model=FLX1s       ro.product.vendor.manufacturer=FuriLabs
#
# so "radon" is the authoritative spelling and the first property
# luneos-device-config reads resolves to it directly. The FLX1s / k6877v1_64 /
# mt6877 symlinks an earlier version of this file carried as insurance are
# unnecessary and have been removed - none of them is what the device reports.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://radon-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/radon
    install -m 0644 ${UNPACKDIR}/radon-deviceinfo \
        ${D}${datadir}/luneos/adaptations/radon/deviceinfo
}
