require recipes-android/android-headers/android-headers.inc

PV = "5.1.0+gitr${SRCPV}"
SRCREV = "eef30c5da666b324072c5b5b029eae71a51d9534"

SRC_URI = "git://github.com/Halium/android-headers.git;branch=master;protocol=git"
ANDROID_API = "22"
S = "${WORKDIR}"

COMPATIBLE_MACHINE = "^mako$"
