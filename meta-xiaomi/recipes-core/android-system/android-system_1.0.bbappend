FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# surya builds no rootfs of its own (it runs the halium-arm64 image, like sargo
# and mp01), so its stub list ships in the generic image under the per-codename
# directory 50-stub-services looks in. The file carries the device in its name
# because meta-google ships sargo's list from the same recipe and the same
# FILESEXTRAPATHS scheme: two layers both adding file://stubbed-services would
# resolve to whichever layer is searched first.
#
# The directory name must match what 50-stub-services derives from the phone's
# own vendor build.prop (ro.product.vendor.device), not from the GSI - which on
# surya is "surya".
SRC_URI:append:halium-arm64 = " file://stubbed-services-surya"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-surya \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/surya
}
