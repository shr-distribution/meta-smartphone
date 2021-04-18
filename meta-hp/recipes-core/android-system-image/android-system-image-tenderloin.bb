require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20210418-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "2776567e6c9fb8759decfc5d511cac96"
SRC_URI[sha256sum] = "c68f48a308fdc22c6ad4ed96d229a164580f62df6a5e2d40e48eed2d2743009e"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
