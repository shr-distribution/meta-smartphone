require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20240313-1"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "9f258461ba71306231400a45fce45539800ad8823e4d50f7c5f9f17d4427d467"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
