FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# sargo and sunfish build no rootfs of their own - see their machine confs - so
# their device-node permissions have to ride in the generic one. Each
# 70-<codename>.rules holds only what 65-android.rules does not already cover,
# and every rule in them keys on a node name, so they match nothing on the
# other devices running this image.
PACKAGE_ARCH:halium-arm64 = "${MACHINE_ARCH}"

SRC_URI:append:halium-arm64 = " file://70-sargo.rules file://70-sunfish.rules"

do_install:append:halium-arm64() {
    install -m 0644 ${UNPACKDIR}/70-sargo.rules ${D}${sysconfdir}/udev/rules.d/70-sargo.rules
    install -m 0644 ${UNPACKDIR}/70-sunfish.rules ${D}${sysconfdir}/udev/rules.d/70-sunfish.rules
}
