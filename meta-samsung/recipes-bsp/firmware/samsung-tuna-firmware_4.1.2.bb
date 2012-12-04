DESCRIPTION = "Properitary firmware binaries needed for the Samsung Galaxy Nexus smartphone"
LICENSE = "Properitary"

ANDROID_VERSION = "4.1.2_r1"
DEVICE_TUNA_NAME = "android_device_samsung_tuna-android-${ANDROID_VERSION}"
HARDWARE_BROADCOM_NAME = "android_hardware_broadcom_wlan-android-${ANDROID_VERSION}"

LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/extract-broadcom-maguro.sh;beginline=16;endline=233;md5=38f6effa6031a775065b10f8943a6efb \
    file://${WORKDIR}/${DEVICE_TUNA_NAME}/README.mms144_ts;md5=dbe38b2af8d17a91e28d0a8f3363a8ad \
    file://${WORKDIR}/${HARDWARE_BROADCOM_NAME}/bcmdhd/firmware/LICENSE.TXT;md5=bafc4300ca5bbd85b704c45969d15e03 \
"

PR = "r2"

COMPATIBLE_MACHINES = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
  https://dl.google.com/dl/android/aosp/broadcom-maguro-jzo54k-8b0d7637.tgz;name=brcm \
  https://github.com/morphis/android_device_samsung_tuna/archive/android-${ANDROID_VERSION}.zip;name=device_tuna;downloadfilename=${DEVICE_TUNA_NAME}.zip \
  https://github.com/morphis/android_hardware_broadcom_wlan/archive/android-${ANDROID_VERSION}.zip;name=hardware_broadcom;downloadfilename=${HARDWARE_BROADCOM_NAME}.zip \
"
S = "${WORKDIR}"

SRC_URI[brcm.md5sum] = "d6e4adf484ac72077015779615f9e59c"
SRC_URI[brcm.sha256sum] = "3e2769eab377ffa4b1a3b35a0d2a49ae12efb371e0d17e8c75e8dd541086af67"
SRC_URI[device_tuna.md5sum] = "5ffe5a52a4fd9b79b5f29ec2696dc354"
SRC_URI[device_tuna.sha256sum] = "5d5d179d5f20ef02bc0426ce59f2320214921cd1bd6a65bc46c95fa9866fd37b"
SRC_URI[hardware_broadcom.md5sum] = "0734ed070b0d0fcc46af47053839b699"
SRC_URI[hardware_broadcom.sha256sum] = "7cd068f48bad939067aa65622bca4416cd7bc28fdd31c6dd3486bef1761b4b10"

FIRMARE_PATH = "/lib/firmware"

BCM4330_BASE_PATH = "${WORKDIR}/${HARDWARE_BROADCOM_NAME}/bcmdhd/firmware/bcm4330"

do_install() {
    tail -n +269 extract-broadcom-maguro.sh | tar zxv
    install -d ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/vendor/broadcom/maguro/proprietary/bcm4330.hcd ${D}${FIRMARE_PATH}

    install -m 0644 ${WORKDIR}/${DEVICE_TUNA_NAME}/bcmdhd.cal ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/${DEVICE_TUNA_NAME}/mms144_ts_rev31.fw ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/${DEVICE_TUNA_NAME}/mms144_ts_rev32.fw ${D}${FIRMARE_PATH}

    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_abg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd.bin
    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_p2p_abg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd_p2p.bin
    install -m 0644 ${BCM4330_BASE_PATH}/fw_bcm4330_apsta_abg.bin ${D}${FIRMARE_PATH}/fw_bcmdhd_apsta.bin
}

# We're differentiate here between files which are redistributable without acknowleding
# any third party license. Files which can be freely redistributes should go into the
# ${PN} package and all other into ${PN}-nonfree

PACKAGES = "${PN}-nonfree ${PN}"
FILES_${PN}-nonfree = " \
    ${FIRMARE_PATH}/bcm4330.hcd \
"
FILES_${PN} = " \
    ${FIRMARE_PATH}/mms144_ts_rev31.fw \
    ${FIRMARE_PATH}/mms144_ts_rev32.fw \
    ${FIRMARE_PATH}/bcmdhd.cal \
    ${FIRMARE_PATH}/fw_bcmdhd.bin \
    ${FIRMARE_PATH}/fw_bcmdhd_p2p.bin \
    ${FIRMARE_PATH}/fw_bcmdhd_apsta.bin \
"
