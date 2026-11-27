FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PR:append:sargo = ".1"
PR:append:sunfish = ".1"
PR:append:bramble = ".1"

# sargo's, sunfish's and bramble's stub lists, under the per-codename directory
# 50-stub-services looks in. The phones run either the generic halium-arm64
# rootfs or an image built for their own MACHINE, so the lists ride in all of
# them; on any other device the codename does not match and the files are
# simply never read. With only the halium-arm64 override, the android-system
# package of a MACHINE=sargo image (15 Nov 2026) carried no stub list at all.
#
# PACKAGE_ARCH is already MACHINE_ARCH, so only these machines' packages change.
GOOGLE_STUB_SRC = " file://stubbed-services file://stubbed-services-sunfish file://stubbed-services-bramble file://vendor-modules-sunfish"
SRC_URI:append:halium-arm64 = "${GOOGLE_STUB_SRC}"
SRC_URI:append:sargo = "${GOOGLE_STUB_SRC}"
SRC_URI:append:sunfish = "${GOOGLE_STUB_SRC}"
SRC_URI:append:bramble = "${GOOGLE_STUB_SRC}"

google_install_stubs() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sargo
    install -m 0644 ${UNPACKDIR}/stubbed-services-sunfish \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/sunfish
    install -m 0644 ${UNPACKDIR}/stubbed-services-bramble \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/bramble

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

do_install:append:halium-arm64() {
    google_install_stubs
}
do_install:append:sargo() {
    google_install_stubs
}
do_install:append:sunfish() {
    google_install_stubs
}
do_install:append:bramble() {
    google_install_stubs
}
