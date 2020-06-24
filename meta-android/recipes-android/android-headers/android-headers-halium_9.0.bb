require recipes-android/android-headers/android-headers.inc

PV = "9.0+gitr${SRCPV}"
SRCREV = "00543b4ff3f56a9f96be336df276365aef46f8a2"

SRC_URI = "git://github.com/halium/android-headers.git;branch=halium-9.0;protocol=git"
ANDROID_API = "28"
S = "${WORKDIR}"
