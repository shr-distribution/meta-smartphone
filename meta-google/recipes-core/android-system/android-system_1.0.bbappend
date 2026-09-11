FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# sargo builds no rootfs of its own - see sargo.conf - so its stub list ships
# in the generic one, under the per-codename directory 50-stub-services looks
# in. On any other device running this image the codename does not match and
# the file is simply never read.
SRC_URI:append:halium-arm64 = " file://stubbed-services"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sargo
}
