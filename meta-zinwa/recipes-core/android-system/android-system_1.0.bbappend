FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The Q25's list of container services to stub out, under the per-codename directory 50-stub-services reads:
# the hook runs before the property service exists and takes the codename from the vendor build.prop, where
# ro.product.vendor.device is "Q25".
SRC_URI:append:q25 = " file://stubbed-services-q25"

# PACKAGE_ARCH is MACHINE_ARCH, so this bumps the revision of the q25 package alone.
PR:append:q25 = ".1"

do_install:append:q25() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-q25 \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/Q25
}
