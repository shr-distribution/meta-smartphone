require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210211-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "9864699ef2045ad457698733c42fdf21"
SRC_URI[sha256sum] = "0dbce3f5907d8405ce74044708ba351bf7b7587fb3effaa0117babe7c8e97327"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
