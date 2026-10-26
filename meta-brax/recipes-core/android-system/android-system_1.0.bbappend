FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The BraX3's list of container services to stub out, under the per-codename
# directory 50-stub-services reads. The hook runs before the property service
# exists and takes the codename from the vendor build.prop, where
# ro.product.vendor.device is "brax3" on iodeOS 7.x and "BraX3" on 6.x, so the
# list is installed under both names.
SRC_URI:append:brax3 = " file://stubbed-services-brax3"

# PACKAGE_ARCH is MACHINE_ARCH, so this bumps the revision of the brax3
# package alone.
PR:append:brax3 = ".1"

do_install:append:brax3() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-brax3 \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/brax3
    ln -sfn brax3 ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/BraX3
}
