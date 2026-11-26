FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The Q25's list of container services to stub out, under the per-codename directory 50-stub-services reads:
# the hook runs before the property service exists and takes the codename from the vendor build.prop, where
# ro.product.vendor.device is "Q25".
SRC_URI:append:q25 = " file://stubbed-services-q25"

# The health HAL without its 10-minute wake alarm, under the per-codename directory 65-extra-init reads
# (see the file itself).
SRC_URI:append:q25 = " file://health-no-wake-alarm-q25.rc"

# PACKAGE_ARCH is MACHINE_ARCH, so this bumps the revision of the q25 package alone.
PR:append:q25 = ".2"

do_install:append:q25() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-q25 \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/Q25
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Q25
    install -m 0644 ${UNPACKDIR}/health-no-wake-alarm-q25.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Q25/health-no-wake-alarm.rc
}
