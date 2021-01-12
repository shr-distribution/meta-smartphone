require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "yggdrasil"

PV = "20210108-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "b79a79d38524fdd24e043291b761016c"
SRC_URI[sha256sum] = "3010b0a041984d845ff64be28d32db67615208e884f587aeac7cd58b1caa4429"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
