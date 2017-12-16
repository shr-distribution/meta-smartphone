FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

require recipes-android/android-headers/android-headers.inc

PV = "7.1+gitr${SRCPV}"
SRCREV = "1f6591ba7d42b91c32acf5a65a2a4fae983d6865"

SRC_URI = "git://github.com/Halium/android-headers.git;branch=halium-7.1;protocol=git"
ANDROID_API = "25"
S = "${WORKDIR}"
