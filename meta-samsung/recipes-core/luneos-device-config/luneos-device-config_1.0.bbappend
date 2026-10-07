# Ship the SM-T220 Tier 1 adaptation. Installed unconditionally like every
# other adaptation - it only takes effect when the running device's codename
# matches - so no COMPATIBLE_MACHINE guard. Named flatly rather than as an
# adaptations/ tree so it does not shadow the base recipe's own adaptations
# directory in the file:// search path.
#
# The directory name has to equal what luneos-device-config derives as the
# codename, which it reads in this order:
#   ro.product.vendor.device -> ro.vendor.product.device -> ro.product.device
#   -> the part after the comma in /proc/device-tree/compatible
# CONFIRMED from Samsung's own firmware (T220XXSAEYE4, super.img -> vendor
# build.prop):
#
#   ro.product.vendor.device=gta7litewifi   ro.product.board=gta7litewifi
#   ro.product.vendor.model=SM-T220         ro.product.vendor.name=gta7litewifixx
#   ro.product.vendor.manufacturer=samsung
#
# so "gta7litewifi" is the first property it reads and resolves directly.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://gta7litewifi-deviceinfo file://n2awifi-deviceinfo file://a3lte-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/gta7litewifi
    install -m 0644 ${UNPACKDIR}/gta7litewifi-deviceinfo \
        ${D}${datadir}/luneos/adaptations/gta7litewifi/deviceinfo
    install -d ${D}${datadir}/luneos/adaptations/n2awifi
    install -m 0644 ${UNPACKDIR}/n2awifi-deviceinfo \
        ${D}${datadir}/luneos/adaptations/n2awifi/deviceinfo
    # a3-2015-halium: the vendor reports a3lte on every A3 (2015) model; see the file.
    install -d ${D}${datadir}/luneos/adaptations/a3lte
    install -m 0644 ${UNPACKDIR}/a3lte-deviceinfo \
        ${D}${datadir}/luneos/adaptations/a3lte/deviceinfo
}
