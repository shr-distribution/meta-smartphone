require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20171130-5"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "155471e00dc12e61745274898103fbd1"
SRC_URI[sha256sum] = "64c6d4c48678d22a6d24ecf8be2e3bdd1867d15ea27c7db03dccbe2d6866cb4f"
