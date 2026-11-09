FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# lagos builds no rootfs of its own - it runs the generic halium-arm64 one, see
# lagos.conf - so its stub list ships in that image, under the per-codename
# directory 50-stub-services reads. The hook runs before the property service
# exists and takes the codename from the vendor build.prop, where
# ro.product.vendor.device is "lagos" (VVOB35.78-188). On any other device the
# codename does not match and the file is never read. Same arrangement as
# meta-google's sargo, sunfish and bramble lists.
SRC_URI:append:halium-arm64 = " file://stubbed-services-lagos"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-lagos \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/lagos
}
