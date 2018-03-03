require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athene"

PV = "20180303-2"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "1103f36923a10e21fa84c494f58e66a8"
SRC_URI[sha256sum] = "195b66b61340be1015628c90106d537c071476b1acb6946bfa2ac9be11a577d2"
