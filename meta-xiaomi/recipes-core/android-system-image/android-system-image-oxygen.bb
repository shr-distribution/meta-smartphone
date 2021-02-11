require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "oxygen"

PV = "20201219-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "92a3bdb64c5a6fff0735bb40024abcad"
SRC_URI[sha256sum] = "7ee780041cdbe95933444f1c3937b38c1de9fb45b8a6cf328e0b95e6e8b47850"
