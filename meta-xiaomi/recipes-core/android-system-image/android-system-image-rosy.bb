require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20210104-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "af0dfa8956a9cf8709be2a526dbe8218"
SRC_URI[sha256sum] = "379af650c65804230d29c203319a526c9cf8da87388ae018db35f9327ad6d70a"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

