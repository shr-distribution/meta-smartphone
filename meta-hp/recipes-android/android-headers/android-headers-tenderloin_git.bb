FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

require recipes-android/android-headers/android-headers.inc

PV = "5.1.0+gitr${SRCPV}"
SRCREV = "eef30c5da666b324072c5b5b029eae71a51d9534"

SRC_URI = "git://github.com/Halium/android-headers.git;branch=master;protocol=git \
           file://0001-Revert-hwc-remove-qcom_bsp-since-it-causes-trouble-w.patch;striplevel=1 \
           file://0002-Revert-Use-AOSP-version-of-gralloc.h.patch;striplevel=1"
ANDROID_API = "22"
S = "${WORKDIR}"

COMPATIBLE_MACHINE = "^tenderloin$"
