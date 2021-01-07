require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20200812-43"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "52ac208b4ae764dd2871a4db7c312cd1"
SRC_URI[sha256sum] = "b67ba12449574373f40fcbed75e06df7abdc8764ae9f8301473cdaaf5c1a55ce"
# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

