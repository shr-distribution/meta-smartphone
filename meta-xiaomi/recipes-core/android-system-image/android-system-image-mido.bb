require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20180914-22"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "90e462bc2ed0e031b9d133c8165f4ea4"
SRC_URI[sha256sum] = "359130adbbe391d0c894fef37e3e37710b9f7ee38a8cc5edfc7c9de7b81917be"
