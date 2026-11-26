FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PR:append:halium-arm64 = ".1"
PR:append:mp01 = ".2"

# mp01's stub list, under the per-codename directory 50-stub-services looks
# in. The phone runs either the generic halium-arm64 rootfs or an image built
# for its own MACHINE, so the list rides in both; on any other device the
# codename does not match and the file is simply never read. With only the
# halium-arm64 override, a MACHINE=mp01 image would carry no stub list (as a
# MACHINE=sargo one did, 15 Nov 2026).
#
# The file carries the device in its name because meta-google ships sargo's
# list from the same recipe and the same FILESEXTRAPATHS scheme: two layers
# both adding file://stubbed-services would resolve to whichever layer is
# searched first.
SRC_URI:append:halium-arm64 = " file://stubbed-services-mp01"
SRC_URI:append:mp01 = " file://stubbed-services-mp01"

# The health HAL without its 10-minute wake alarm, for 65-extra-init (see the file itself).
SRC_URI:append:halium-arm64 = " file://health-no-wake-alarm-mp01.rc"
SRC_URI:append:mp01 = " file://health-no-wake-alarm-mp01.rc"

minimal_install_stubs() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-mp01 \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/MP01
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/MP01
    install -m 0644 ${UNPACKDIR}/health-no-wake-alarm-mp01.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/MP01/health-no-wake-alarm.rc
}

do_install:append:halium-arm64() {
    minimal_install_stubs
}
do_install:append:mp01() {
    minimal_install_stubs
}
