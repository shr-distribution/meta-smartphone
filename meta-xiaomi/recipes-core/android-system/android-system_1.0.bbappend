FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PR:append:surya = ".1"

# surya's stub list, under the per-codename directory 50-stub-services looks
# in. The phone runs either the generic halium-arm64 rootfs or an image built
# for its own MACHINE, so the list rides in both; on any other device the
# codename does not match and the file is simply never read. With only the
# halium-arm64 override, a MACHINE=surya image would carry no stub list (as a
# MACHINE=sargo one did, 15 Nov 2026).
#
# The file carries the device in its name because meta-google ships sargo's
# list from the same recipe and the same FILESEXTRAPATHS scheme: two layers
# both adding file://stubbed-services would resolve to whichever layer is
# searched first.
#
# The directory name must match what 50-stub-services derives from the phone's
# own vendor build.prop (ro.product.vendor.device), not from the GSI - which on
# surya is "surya".
SRC_URI:append:halium-arm64 = " file://stubbed-services-surya"
SRC_URI:append:surya = " file://stubbed-services-surya"

xiaomi_install_stubs() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-surya \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/surya
}

do_install:append:halium-arm64() {
    xiaomi_install_stubs
}
do_install:append:surya() {
    xiaomi_install_stubs
}
