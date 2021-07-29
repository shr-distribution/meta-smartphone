DESCRIPTION = "Additional sub-initramfs for booting into recovery on A/B android devices"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS += "abootimg-native"
PROVIDES = "initramfs-android-recovery"
RPROVIDES:${PN} = "initramfs-android-recovery"

PACKAGES = "${PN}"
FILES:${PN} = "/recovery"
RECOVERY_IMG ?= "recovery.img"

do_install() {
    cd ${WORKDIR}
    abootimg -x ${RECOVERY_IMG}
    abootimg-unpack-initrd initrd.img ${D}/recovery
}
