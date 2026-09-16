FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# mp01 builds no rootfs of its own - see mp01.conf - so its device-node
# permissions and input tagging have to ride in the generic halium-arm64 one,
# exactly as sargo's and q25's do. 70-mp01.rules holds only what
# 65-android.rules does not already cover, and every rule in it keys on a node
# name or a device name, so it matches nothing on the other devices running
# this image.
PACKAGE_ARCH:halium-arm64 = "${MACHINE_ARCH}"

SRC_URI:append:halium-arm64 = " file://70-mp01.rules"

do_install:append:halium-arm64() {
    install -m 0644 ${UNPACKDIR}/70-mp01.rules ${D}${sysconfdir}/udev/rules.d/70-mp01.rules
}
