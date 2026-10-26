FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# The BraX3's device-node permissions that 65-android.rules does not cover;
# see 70-brax3.rules. Machine-specific, so the package becomes so too.
PACKAGE_ARCH:brax3 = "${MACHINE_ARCH}"

SRC_URI:append:brax3 = " file://70-brax3.rules"

do_install:append:brax3() {
    install -m 0644 ${UNPACKDIR}/70-brax3.rules ${D}${sysconfdir}/udev/rules.d/70-brax3.rules
}
