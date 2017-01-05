FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

require recipes-android/android-headers/android-headers.inc

PV = "4.4.3+gitr${SRCPV}"
SRCREV = "957ab6e28aea03d0cf6495f33ade9ddfff480ccc"

SRC_URI = "git://git.launchpad.net/android-headers;branch=master;protocol=git \
           file://0001-API-19-add-old-hwcomposer-APIs-for-TP-compatibility.patch;striplevel=2 \
           "
ANDROID_API = "19"
S = "${WORKDIR}/git/${ANDROID_API}"

COMPATIBLE_MACHINE = "^tenderloin$"
