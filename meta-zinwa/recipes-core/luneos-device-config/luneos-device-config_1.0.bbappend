# Ship the Zinwa Q25 Tier 1 adaptation. Installed unconditionally like every
# other adaptation - it only takes effect when the running device's codename
# matches - so no COMPATIBLE_MACHINE guard. Named flatly rather than as an
# adaptations/ tree so it does not shadow the base recipe's own adaptations
# directory in the file:// search path.
#
# The directory name has to equal what luneos-device-config derives as the
# codename, which it reads from ro.product.vendor.device, then
# ro.vendor.product.device, then ro.product.device, then the device tree's
# compatible. "Q25" - capital Q - is PRODUCT_DEVICE in LineageOS's
# device/xelex/Q25, and the device's own kernel names its keyboard
# "Q25_keyboard", so that is the authoritative spelling.
#
# CONFIRMED against the stock firmware's own vendor partition properties
# (OS-new-camera-0120-Q25-GMS.zip, q20_v12_factory/vendor.prop):
# ro.product.vendor.device=Q25 - which is the first property
# luneos-device-config looks at. The build *fingerprint* on that ROM says
# Xelex10_Ultra, but that is an attestation spoof and not what is read here.
# The lowercase symlink stays as cheap insurance against a future ROM changing
# the case.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://Q25-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/Q25
    install -m 0644 ${UNPACKDIR}/Q25-deviceinfo \
        ${D}${datadir}/luneos/adaptations/Q25/deviceinfo
    ln -sfn Q25 ${D}${datadir}/luneos/adaptations/q25
}
