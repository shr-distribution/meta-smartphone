FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

PR:append:halium-arm64 = ".1"

# Init scripts for the Titan Pocket's and the Titan's containers, under the per-codename
# directories 65-extra-init reads (ro.product.vendor.device is "Titan_pocket" and "Titan").
# Both run the generic halium-arm64 rootfs, so this rides in that image, as udev-extraconf's
# 70-titanpocket.rules does; on any other device the directory's name matches nothing and it is
# never read. PACKAGE_ARCH is already MACHINE_ARCH, so only the halium-arm64 package changes.
SRC_URI:append:halium-arm64 = " file://trustkernel-unencrypted.rc file://trustkernel-unencrypted-titan.rc"

do_install:append:halium-arm64() {
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_pocket
    install -m 0644 ${UNPACKDIR}/trustkernel-unencrypted.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan_pocket/
    install -d ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan
    install -m 0644 ${UNPACKDIR}/trustkernel-unencrypted-titan.rc \
        ${D}${localstatedir}/lib/lxc/android/extra-init.d/Titan/trustkernel-unencrypted.rc
}
