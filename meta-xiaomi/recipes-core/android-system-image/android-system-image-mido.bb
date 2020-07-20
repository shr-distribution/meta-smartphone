require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20200711-42"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "d2aa0bdbf0ad860c19ae858029b9b96d"
SRC_URI[sha256sum] = "43df604a4f3f9c288a2ca6d499ef6fe937569cbd2d88b9708c37d2e72116bda5"
