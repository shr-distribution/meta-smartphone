require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20180303-2"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7a0bc4afac084bd5035ace65f95de0d9"
SRC_URI[sha256sum] = "df93b14c9858efb8cb92789373f790748a6c625f9ecad7ac89b3f586805cb71b"
