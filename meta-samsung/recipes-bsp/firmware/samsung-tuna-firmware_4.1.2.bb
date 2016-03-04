DESCRIPTION = "Proprietary firmware binaries needed for the Samsung Galaxy Nexus smartphone"
LICENSE = "Proprietary"

ANDROID_VERSION = "android-4.1.2_r1"

LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/LICENSE.broadcom;md5=38f6effa6031a775065b10f8943a6efb \
    file://${WORKDIR}/git/device_samsung_tuna/README.mms144_ts;md5=dbe38b2af8d17a91e28d0a8f3363a8ad \
    file://${WORKDIR}/git/hardware_ti_omap4xxx/MODULE_LICENSE_BSD_APL2;md5=d41d8cd98f00b204e9800998ecf8427e \
"

PR = "r6"

# maybe other devices too, but it has tuna in name and 
# we don't want to build it e.g. as part of qemuarm world build
COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "bcm4330-firmware"

SRC_URI = " \
  https://dl.google.com/dl/android/aosp/broadcom-maguro-jzo54k-8b0d7637.tgz;name=brcm \
  git://android.googlesource.com/platform/hardware/ti/omap4xxx;tag=${ANDROID_VERSION};protocol=https;destsuffix=git/hardware_ti_omap4xxx \
  git://android.googlesource.com/device/samsung/tuna;tag=${ANDROID_VERSION};protocol=https;destsuffix=git/device_samsung_tuna \
"
S = "${WORKDIR}"

SRC_URI[brcm.md5sum] = "d6e4adf484ac72077015779615f9e59c"
SRC_URI[brcm.sha256sum] = "3e2769eab377ffa4b1a3b35a0d2a49ae12efb371e0d17e8c75e8dd541086af67"

unpack_broadcom_license() {
    head -n 233 extract-broadcom-maguro.sh | tail -n 218 > LICENSE.broadcom
}

python do_unpack() {
    bb.build.exec_func('base_do_unpack', d)
    bb.build.exec_func('unpack_broadcom_license', d)
}

FIRMARE_PATH = "/lib/firmware"

do_install() {
    tail -n +269 extract-broadcom-maguro.sh | tar zxv
    install -d ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/vendor/broadcom/maguro/proprietary/bcm4330.hcd ${D}${FIRMARE_PATH}

    install -m 0644 ${WORKDIR}/git/device_samsung_tuna/bcmdhd.cal ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/git/device_samsung_tuna/mms144_ts_rev31.fw ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/git/device_samsung_tuna/mms144_ts_rev32.fw ${D}${FIRMARE_PATH}

    install -m 0644 ${WORKDIR}/git/hardware_ti_omap4xxx/domx/Ducati_binary/etc/firmware/ducati-m3.bin ${D}${FIRMARE_PATH}/ducati-m3.bin
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
    ${FIRMARE_PATH}/ducati-m3.bin \
"
