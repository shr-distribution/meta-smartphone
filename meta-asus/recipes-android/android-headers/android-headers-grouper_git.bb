require recipes-android/android-headers/android-headers.inc

PV = "4.2.2+gitr${SRCPV}"
SRCREV = "957ab6e28aea03d0cf6495f33ade9ddfff480ccc"

SRC_URI = "git://git.launchpad.net/android-headers;branch=master;protocol=git"
ANDROID_API = "19"
S = "${WORKDIR}/git/${ANDROID_API}"

COMPATIBLE_MACHINE = "^grouper$"
