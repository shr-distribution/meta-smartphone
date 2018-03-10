require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20180308-9"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "dcf95063c8ee4ce2beb009313fe9316c"
SRC_URI[sha256sum] = "79a64c45c8cf045870e744a537e07fa5fd4c9c0b415e977af24ce0d412b86d5a"
