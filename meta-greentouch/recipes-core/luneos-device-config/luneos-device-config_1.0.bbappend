# Ship the mindphone (codename "mindset") Tier 1 adaptation. Installed
# unconditionally like every other adaptation - it only takes effect when the
# running device's codename matches - so no COMPATIBLE_MACHINE guard. Named
# flatly rather than as an adaptations/ tree so it does not shadow the base
# recipe's own adaptations directory in the file:// search path.
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "file://mindset-deviceinfo"

do_install:append() {
    install -d ${D}${datadir}/luneos/adaptations/mindset
    install -m 0644 ${UNPACKDIR}/mindset-deviceinfo \
        ${D}${datadir}/luneos/adaptations/mindset/deviceinfo
}
