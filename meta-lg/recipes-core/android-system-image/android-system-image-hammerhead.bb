require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20210405-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7bff9d04b897557085ddf10ed88daef2"
SRC_URI[sha256sum] = "9c65d3b65cead0b35fb8fd48dd6859ef50d522e4efed7760dc3c6902fa62259c"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
