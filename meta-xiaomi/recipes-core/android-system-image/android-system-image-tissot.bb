require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20200812-43"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "f04fcbaa68095857d1d79c41ad5f7842"
SRC_URI[sha256sum] = "11eaaa6ab630467ff2b26fce0a64e1293d8f505dee9aa0126b3f877d7f13ca6c"
