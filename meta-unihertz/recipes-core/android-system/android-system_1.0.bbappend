FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PR:append:halium-arm64 = ".3"
PR:append:titan = ".2"
PR:append:titanpocket = ".2"
PR:append:titanslim = ".1"

# Init scripts for the Titan Pocket's, the Titan Slim's and the Titan's containers, under the
# per-codename directories 65-extra-init reads (ro.product.vendor.device is "Titan_pocket",
# "Titan_Slim" and "Titan"). The Slim's stock trustkernel.rc (Android 11, 2022-08-10, read off
# the device 15 Nov 2026) has the Pocket's shape - /data/vendor/t6 only from the encrypted and
# unencrypted ro.crypto.state triggers - so it takes the Pocket's script.
#
# The phones run either the generic halium-arm64 rootfs or an image built for their own
# MACHINE, so the scripts ride in all of them; on any other device the directory's name matches
# nothing and it is never read. With only the halium-arm64 override, a MACHINE=titan image
# (15 Nov 2026) had an empty extra-init.d: nothing set vendor.trustkernel.fs.state, so
# /data/vendor/t6 was never made, the fingerprint HAL hung in tee_supp_cmd() waiting for
# teed, biomd timed out on every start and fingerprint was dead - the very thing
# trustkernel-unencrypted-titan.rc fixed - and the configstore HAL crash-looped again.
#
# The health HAL overrides take its wake alarm away on the Titan and the Pocket, whose vendor rc files
# they copy. The Slim gets none: its vendor rc has not been read, and an override has to match the
# vendor's service name and binary.
#
# PACKAGE_ARCH is already MACHINE_ARCH, so only these machines' packages change.
UNIHERTZ_EXTRA_INIT_SRC = " file://trustkernel-unencrypted.rc file://trustkernel-unencrypted-titan.rc \
    file://vendor-configstore-off-titan.rc file://health-no-wake-alarm-titan.rc \
    file://health-no-wake-alarm-pocket.rc"
SRC_URI:append:halium-arm64 = "${UNIHERTZ_EXTRA_INIT_SRC}"
SRC_URI:append:titan = "${UNIHERTZ_EXTRA_INIT_SRC}"
SRC_URI:append:titanpocket = "${UNIHERTZ_EXTRA_INIT_SRC}"
SRC_URI:append:titanslim = "${UNIHERTZ_EXTRA_INIT_SRC}"

unihertz_install_extra_init() {
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_pocket
    install -m 0644 ${UNPACKDIR}/trustkernel-unencrypted.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_pocket/
    install -m 0644 ${UNPACKDIR}/health-no-wake-alarm-pocket.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_pocket/health-no-wake-alarm.rc
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_Slim
    install -m 0644 ${UNPACKDIR}/trustkernel-unencrypted.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_Slim/
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan
    install -m 0644 ${UNPACKDIR}/trustkernel-unencrypted-titan.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan/trustkernel-unencrypted.rc
    install -m 0644 ${UNPACKDIR}/vendor-configstore-off-titan.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan/vendor-configstore-off.rc
    install -m 0644 ${UNPACKDIR}/health-no-wake-alarm-titan.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan/health-no-wake-alarm.rc
}

do_install:append:halium-arm64() {
    unihertz_install_extra_init
}
do_install:append:titan() {
    unihertz_install_extra_init
}
do_install:append:titanpocket() {
    unihertz_install_extra_init
}
do_install:append:titanslim() {
    unihertz_install_extra_init
}
