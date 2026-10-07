FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The TouchPad's stub list, under the codename the vendor build.prop gives (tenderloin); see
# meta-android's 50-stub-services. Only the GSI image has a container that needs it, but the list is
# inert where the binaries it names do not exist.
SRC_URI:append:tenderloin-halium = " file://stubbed-services-tenderloin"

# PACKAGE_ARCH is MACHINE_ARCH, so this bumps the revision of the tenderloin-halium package alone.
PR:append:tenderloin-halium = ".1"

do_install:append:tenderloin-halium() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-tenderloin \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/tenderloin
}
