DESCRIPTION = "Properitary firmware binaries needed for the Samsung Galaxy Nexus smartphone"
LICENSE = "Properitary"
LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/extract-broadcom-maguro.sh;beginline=16;endline=233;md5=38f6effa6031a775065b10f8943a6efb \
    file://${WORKDIR}/android_device_samsung_tuna-jellybean/README.mms144_ts;md5=dbe38b2af8d17a91e28d0a8f3363a8ad \
"

PR = "r1"

COMPATIBLE_MACHINES = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
    https://dl.google.com/dl/android/aosp/broadcom-maguro-jzo54k-8b0d7637.tgz;name=brcm \
    https://github.com/CyanogenMod/android_device_samsung_tuna/archive/jellybean.zip;name=cm-device-tuna \
"
S = "${WORKDIR}"

SRC_URI[brcm.md5sum] = "d6e4adf484ac72077015779615f9e59c"
SRC_URI[brcm.sha256sum] = "3e2769eab377ffa4b1a3b35a0d2a49ae12efb371e0d17e8c75e8dd541086af67"
SRC_URI[cm-device-tuna.md5sum] = "c1dc7b765e873ebdfb22c095e94d37b9"
SRC_URI[cm-device-tuna.sha256sum] = "be71f394b39231ddf7e37b976411f49cd94b3e57fbda9786bf74e42fc09790d5"

do_install() {
    tail -n +269 extract-broadcom-maguro.sh | tar zxv
    install -d ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/vendor/broadcom/maguro/proprietary/bcm4330.hcd ${D}/lib/firmware/fw_bcmdhd.bin
    install -m 0644 ${WORKDIR}/android_device_samsung_tuna-jellybean/bcmdhd.cal ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/android_device_samsung_tuna-jellybean/mms144_ts_rev31.fw ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/android_device_samsung_tuna-jellybean/mms144_ts_rev32.fw ${D}/lib/firmware
}

PACKAGES = "${PN}"
FILES_${PN} = "/lib/firmware"
