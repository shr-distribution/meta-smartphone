require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20210501-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "651fe94d89e6a757d149796f1f9dcf923073cedbb1d8aefb77c8933ed42d9b7d"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
