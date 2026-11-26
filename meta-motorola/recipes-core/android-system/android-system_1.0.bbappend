FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# lagos normally runs the generic halium-arm64 rootfs - see lagos.conf - so its
# stub list ships in that image, under the per-codename directory
# 50-stub-services reads. The hook runs before the property service exists and
# takes the codename from the vendor build.prop, where ro.product.vendor.device
# is "lagos" (VVOB35.78-188). On any other device the codename does not match
# and the file is never read. Same arrangement as meta-google's sargo, sunfish
# and bramble lists.
#
# MACHINE=lagos builds a rootfs of its own too, for
# luneos-dev-fastboot-package, and that image needs the list just the same:
# without it the three services in it crash-loop or spin again, as on the
# first boot on hardware.
LAGOS_STUBBED_SERVICES = "0"
LAGOS_STUBBED_SERVICES:halium-arm64 = "1"
LAGOS_STUBBED_SERVICES:lagos = "1"

SRC_URI:append = "${@' file://stubbed-services-lagos' if d.getVar('LAGOS_STUBBED_SERVICES') == '1' else ''}"

do_install:append() {
    if [ "${LAGOS_STUBBED_SERVICES}" = "1" ]; then
        install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
        install -m 0644 ${UNPACKDIR}/stubbed-services-lagos \
            ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/lagos
    fi
}
