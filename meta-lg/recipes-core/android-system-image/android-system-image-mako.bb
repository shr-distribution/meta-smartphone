require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20210501-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "d80d7c711189577e74eeaa6767d6fc73e254ac5be54d1d701849bc80492846e2"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
