# Ship the Nexus 7 (2013) Wi-Fi (flo-halium) Tier 1 adaptation. Installed unconditionally like
# every other adaptation - it only takes effect when the running device's codename matches - so
# no COMPATIBLE_MACHINE guard. Named flatly rather than as an adaptations/ tree so it does not
# shadow the base recipe's own adaptations directory in the file:// search path.
#
# The directory name has to equal what luneos-device-config derives as the codename, which it
# reads from ro.product.vendor.device first: the LineageOS 18.1 vendor of this machine sets it to
# flox (its vendor build.prop), not flo.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://flox-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/flox
    install -m 0644 ${UNPACKDIR}/flox-deviceinfo \
        ${D}${datadir}/luneos/adaptations/flox/deviceinfo
}
