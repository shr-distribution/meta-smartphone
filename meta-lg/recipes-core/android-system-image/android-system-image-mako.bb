require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20210419-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "415aa2bbfd5f2085c1fe296cbf52bced"
SRC_URI[sha256sum] = "70eb3334321fc3f42f035fda37323efa8b7bd1eb44d1e888b2fdd800d73d562e"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
