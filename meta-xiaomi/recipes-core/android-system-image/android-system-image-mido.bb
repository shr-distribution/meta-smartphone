require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20210218-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "ad0059adecc11ec320697a8abbb3da67"
SRC_URI[sha256sum] = "5fd7a90795be316cfd01bc6784b1bf0c8ef1d54c6870bc882b2c677a062d916d"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
