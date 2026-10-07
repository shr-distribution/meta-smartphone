FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# Unlike sargo and the MP01, the mindphone builds a rootfs of its own, so the
# list could just as well be installed as the single stubbed-services file
# 50-stub-services falls back to. It goes under the per-codename directory
# anyway: the name then says which phone it describes, which matters here
# because the machine is "mindphone" while the vendor build.prop - the only
# thing the hook can read, running before the property service exists - calls
# the device "mindset".
SRC_URI:append:mindphone = " file://stubbed-services-mindphone"

# PACKAGE_ARCH is MACHINE_ARCH, so this bumps the revision of the mindphone
# package alone and leaves every other machine's sstate untouched.
PR:append:mindphone = ".3"

do_install:append:mindphone() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-mindphone \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/mindset
}
