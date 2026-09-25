FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# sargo and sunfish build no rootfs of their own - see their machine confs - so
# their stub lists ship in the generic one, under the per-codename directory
# 50-stub-services looks in. On any other device running this image the
# codename does not match and the files are simply never read.
SRC_URI:append:halium-arm64 = " file://stubbed-services file://stubbed-services-sunfish file://vendor-modules-sunfish"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sargo
    install -m 0644 ${UNPACKDIR}/stubbed-services-sunfish \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sunfish

    # sunfish force-loads the stock vendor modules: pre-GKI Qualcomm, so the
    # drivers that matter (wlan, the audio dlkm stack, the haptics, and the
    # adsp loader that boots the DSPs) are all .ko on the vendor partition, and
    # a kernel rebuilt with luneos.cfg refuses every one of them. The file is
    # the opt-in; 45-vendor-modules does the work. Same per-codename
    # arrangement, and for the same reason, as the stub lists above.
    install -d ${D}${localstatedir}/lib/lxc/android/vendor-modules.d
    install -m 0644 ${UNPACKDIR}/vendor-modules-sunfish \
        ${D}${localstatedir}/lib/lxc/android/vendor-modules.d/sunfish
}
