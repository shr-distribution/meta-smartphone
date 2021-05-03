require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20210502-5"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "f50c55476184276b238db6b0299161d0c1890b2032ff9cea032f651be6ac9269"
