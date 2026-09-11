FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# sargo builds no rootfs of its own - see sargo.conf - so its device-node
# permissions have to ride in the generic one. 70-sargo.rules holds only what
# 65-android.rules does not already cover, and every rule in it keys on a node
# name, so it matches nothing on the other devices running this image.
PACKAGE_ARCH:halium-arm64 = "${MACHINE_ARCH}"

SRC_URI:append:halium-arm64 = " file://70-sargo.rules"

do_install:append:halium-arm64() {
    install -m 0644 ${UNPACKDIR}/70-sargo.rules ${D}${sysconfdir}/udev/rules.d/70-sargo.rules
}
