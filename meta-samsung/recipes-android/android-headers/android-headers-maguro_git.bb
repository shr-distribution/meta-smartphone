require recipes-android/android-headers/android-headers.inc

PV = "5.1.1+gitr${SRCPV}"
SRCREV = "957ab6e28aea03d0cf6495f33ade9ddfff480ccc"

SRC_URI = "git://git.launchpad.net/android-headers;branch=master;protocol=git"
ANDROID_API = "22"
S = "${WORKDIR}/git/${ANDROID_API}"

COMPATIBLE_MACHINE = "^maguro$"
