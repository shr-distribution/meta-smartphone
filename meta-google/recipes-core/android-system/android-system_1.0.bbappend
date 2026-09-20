FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# sargo and sunfish build no rootfs of their own - see their machine confs - so
# their stub lists ship in the generic one, under the per-codename directory
# 50-stub-services looks in. On any other device running this image the
# codename does not match and the files are simply never read.
SRC_URI:append:halium-arm64 = " file://stubbed-services file://stubbed-services-sunfish"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sargo
    install -m 0644 ${UNPACKDIR}/stubbed-services-sunfish \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sunfish
}
