FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The Titan Pocket runs the generic halium-arm64 rootfs and builds only a boot
# image of its own, so its device-node permissions ride in the generic image,
# as q25's and sargo's do. Every rule in 70-titanpocket.rules keys on a node
# name, so it matches nothing on the other devices running this image.
PACKAGE_ARCH:halium-arm64 = "${MACHINE_ARCH}"

SRC_URI:append:halium-arm64 = " file://70-titanpocket.rules"

do_install:append:halium-arm64() {
    install -m 0644 ${UNPACKDIR}/70-titanpocket.rules ${D}${sysconfdir}/udev/rules.d/70-titanpocket.rules
}
