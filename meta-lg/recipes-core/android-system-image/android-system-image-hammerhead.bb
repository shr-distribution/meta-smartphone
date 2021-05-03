require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20210429-18"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "5e05d183a1d20e4801fadb7a8b225f9051f39623c08bf642732c0b5aae7652db"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
