require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20210309-2"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e92df1df9da4907fd85d2648b5574c17"
SRC_URI[sha256sum] = "5caa0dfcc4960f29d6d9f7389637823272a114681e08bbdfab418beee04f12de"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
