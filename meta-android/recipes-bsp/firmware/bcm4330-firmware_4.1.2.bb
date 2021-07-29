DESCRIPTION = "Proprietary firmware binaries needed for the BCM4330 wifi/bt chip"
LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://${WORKDIR}/${HARDWARE_BROADCOM_NAME}/bcmdhd/firmware/LICENSE.TXT;md5=bafc4300ca5bbd85b704c45969d15e03"
PR = "r2"

ANDROID_VERSION = "4.1.2_r1"
HARDWARE_BROADCOM_NAME = "android_hardware_broadcom_wlan-android-${ANDROID_VERSION}"

COMPATIBLE_MACHINE = "tuna|grouper"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = "https://github.com/webOS-ports/android_hardware_broadcom_wlan/archive/android-${ANDROID_VERSION}.zip;name=hardware_broadcom;downloadfilename=${HARDWARE_BROADCOM_NAME}.zip"

S = "${WORKDIR}"

SRC_URI[hardware_broadcom.md5sum] = "0734ed070b0d0fcc46af47053839b699"
SRC_URI[hardware_broadcom.sha256sum] = "7cd068f48bad939067aa65622bca4416cd7bc28fdd31c6dd3486bef1761b4b10"

FIRMARE_PATH = "/lib/firmware"

BCM4330_BASE_PATH = "${WORKDIR}/${HARDWARE_BROADCOM_NAME}/bcmdhd/firmware/bcm4330"

do_install:append() {
    install -d ${D}${FIRMARE_PATH}
    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_bg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd.bin
    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_p2p_bg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd_p2p.bin
    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_apsta_bg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd_apsta.bin
}

PACKAGES = "${PN}"
FILES:${PN} = " \
    ${FIRMARE_PATH}/fw_bcmdhd.bin \
    ${FIRMARE_PATH}/fw_bcmdhd_p2p.bin \
    ${FIRMARE_PATH}/fw_bcmdhd_apsta.bin \
"
