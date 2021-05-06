require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20210226-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "bb4f10800a23af88970faa9bb8e4cc8b"
SRC_URI[sha256sum] = "965fd66f7ceb7574e3ded4f533d95330a0b39b9b841f354074f6e21065009a5a"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

