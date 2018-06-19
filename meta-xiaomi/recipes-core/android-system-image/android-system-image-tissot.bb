require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180311-10"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "878b45c77827a72cf34423b2e876f55e"
SRC_URI[sha256sum] = "0bc719a093781870e91d799ef4d5670d6fc80f37074c0b9c0a2d88921fd47799"
