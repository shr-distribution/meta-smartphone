FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

require recipes-android/android-headers/android-headers.inc

PV = "8.1+gitr${SRCPV}"
SRCREV = "9d2f25e13880932d5172c812f4706068c7a178bb"

SRC_URI = "git://github.com/halium/android-headers.git;branch=halium-8.1;protocol=git"
ANDROID_API = "27"
S = "${WORKDIR}"
