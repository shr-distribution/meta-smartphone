require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20210501-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "41db0a1197512093b58d56d1e07827e1333f8e1cd3120917ba19fb2d5a59aa10"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

