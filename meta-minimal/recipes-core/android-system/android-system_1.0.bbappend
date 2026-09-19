FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# mp01 builds no rootfs of its own (it runs the halium-arm64 image, like
# sargo), so its stub list ships in the generic image under the per-codename
# directory 50-stub-services looks in. The file carries the device in its
# name because meta-google ships sargo's list from the same recipe and the
# same FILESEXTRAPATHS scheme: two layers both adding file://stubbed-services
# would resolve to whichever layer is searched first.
SRC_URI:append:halium-arm64 = " file://stubbed-services-mp01"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/stubbed-services.d
    install -m 0644 ${UNPACKDIR}/stubbed-services-mp01 \
        ${D}${localstatedir}/lib/lxc/android/stubbed-services.d/MP01
}
