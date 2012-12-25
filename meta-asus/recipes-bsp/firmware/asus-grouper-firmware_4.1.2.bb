DESCRIPTION = "Proprietary firmware binaries needed for the ASUS Grouper tablet device"
LICENSE = "Proprietary"

ANDROID_VERSION = "4.1.2_r1"

LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/LICENSE.broadcom;md5=38f6effa6031a775065b10f8943a6efb \
    file://${WORKDIR}/LICENSE.elan;md5=0f6d538952b84a72bb98a8631b749d12 \
    file://${WORKDIR}/LICENSE.nvidia;md5=a7760e2d42bec19a59a14fa084df0d39 \
"

PR = "r0"

COMPATIBLE_MACHINES = "grouper"
PACKAGE_ARCH = "${MACHINE_ARCH}"

RDEPENDS_${PN} = "bcm4330-firmware"

SRC_URI = " \
  https://dl.google.com/dl/android/aosp/nvidia-grouper-jzo54k-56de148f.tgz;name=nvidia \
  https://dl.google.com/dl/android/aosp/elan-grouper-jzo54k-c889b8f4.tgz;name=elan \
  https://dl.google.com/dl/android/aosp/broadcom-grouper-jzo54k-26240daf.tgz;name=brcm \
"
S = "${WORKDIR}"

SRC_URI[nvidia.md5sum] = "c2fc0baa77ee1f9a71096459950651ba"
SRC_URI[nvidia.sha256sum] = "41c2fc49bbdd8956710fd451984403ca61bef41e2b3e80bce46e8a640ecd3957"
SRC_URI[elan.md5sum] = "a49f164ed500c973719f2acf99812c02"
SRC_URI[elan.sha256sum] = "4980441e4a159723c559884ecbf1f085a8524b0b8ee87478df82589da69f48a9"
SRC_URI[brcm.md5sum] = "d9f54118d7dc467bea232e20017290a8"
SRC_URI[brcm.sha256sum] = "807d8802aed427685fa73e2a8c70b10bffa5bf649b6a1a239553e9f82c1f3a75"

unpack_licenses() {
    head -n 233 extract-broadcom-grouper.sh | tail -n 218 > LICENSE.broadcom
    head -n 233 extract-elan-grouper.sh | tail -n 218 > LICENSE.elan
    head -n 233 extract-nvidia-grouper.sh | tail -n 218 > LICENSE.nvidia
}

python do_unpack() {
    bb.build.exec_func('base_do_unpack', d)
    bb.build.exec_func('unpack_licenses', d)
}

FIRMARE_PATH = "/lib/firmware"

do_install() {
    tail -n +269 extract-broadcom-grouper.sh | tar zxv
    tail -n +269 extract-elan-grouper.sh | tar zxv
    tail -n +269 extract-nvidia-grouper.sh | tar zxv

    install -d ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/vendor/broadcom/grouper/proprietary/bcm4330.hcd ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/vendor/elan/grouper/proprietary/touch_fw.ekt ${D}${FIRMARE_PATH}
    install -m 0644 ${WORKDIR}/vendor/nvidia/grouper/proprietary/nvram.txt ${D}${FIRMARE_PATH}
}

PACKAGES = "${PN}"
FILES_${PN} = " \
    ${FIRMARE_PATH}/bcm4330.hcd \
    ${FIRMARE_PATH}/touch_fw.ekt \
    ${FIRMARE_PATH}/nvram.txt \
"
