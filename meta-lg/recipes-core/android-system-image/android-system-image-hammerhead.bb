require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20181023-19"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "03e4379ae9e2adca351ab797f7d9bda8"
SRC_URI[sha256sum] = "36dec20dbbb5f3e9f308c9a5f25e770cd478a8f1057860080a8e2bc3023e45eb"
