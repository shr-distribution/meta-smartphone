require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20200711-42"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "fa8723e611b5806c8c5462ad40423279"
SRC_URI[sha256sum] = "a0b76337af589984a9053fbf498c6bd550aa343cee766fd5cdfff8ddeaa74147"
