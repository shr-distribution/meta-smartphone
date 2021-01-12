require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "yggdrasil"

PV = "20210112-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "b03137c922634deafa778240622a2f1b"
SRC_URI[sha256sum] = "e4d60d5687c2ece1829c82c70c2d8abbe0e6b7a1ecb236fb137979af9c8dcf8a"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
