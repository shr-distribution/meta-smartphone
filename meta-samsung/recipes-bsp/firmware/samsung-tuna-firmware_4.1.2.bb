DESCRIPTION = "Properitary firmware binaries needed for the Samsung Galaxy Nexus smartphone"
LICENSE = "Properitary"
LIC_FILES_CHKSUM = " \
    file://extract-broadcom-maguro.sh;beginline=16;endline=233;md5=38f6effa6031a775065b10f8943a6efb \
    file://aosp-device-tuna/README-mms144_ts;md5=dbe38b2af8d17a91e28d0a8f3363a8ad \
"

PR = "r0"

COMPATIBLE_MACHINES = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

ANDROID_VERSION_TAG = "android-4.1.2_r1"

SRC_URI = " \
    https://dl.google.com/dl/android/aosp/broadcom-maguro-jzo54k-8b0d7637.tgz;name=brcm \
    git://android.googlesource.com/platform/device/samsung/tuna;dest-suffix=aosp-device-tuna;tag=${ANDROID_VERSION_TAG};protocol=https \
"

SRC_URI[brcm.md5sum] = "d6e4adf484ac72077015779615f9e59c"
SRC_URI[brcm.sha256sum] = "3e2769eab377ffa4b1a3b35a0d2a49ae12efb371e0d17e8c75e8dd541086af67"

do_install() {
    tail -n +269 extract-imgtec-maguro.sh | tar zxv

    install -d ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/vendor/broadcom/maguro/properitary/bcm4330.hcd ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/aosp-device-tuna/bcmdhd.cal ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/aosp-device-tuna/mms144_ts_rev31.fw ${D}/lib/firmware
    install -m 0644 ${WORKDIR}/aosp-device-tuna/mms144_ts_rev32.fw ${D}/lib/firmware
}

PACKAGES = "${PN}"
FILES_${PN} = "/lib/firmware"
