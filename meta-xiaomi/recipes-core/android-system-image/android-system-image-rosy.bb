require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

# PV went backwards due to loss of image on file server. Using previous build from backup.
# PV = "20210506-2"

PV = "20210219-1"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
# SRC_URI[sha256sum] = "24ff90a6c790e360df35306a6e0d2f572f7a1c4ebdf9de9c3bfc371465ccfed5"
SRC_URI[sha256sum] = "6490047a5ebc33f1328b1af63bb530a8be4bdfe919be278f3fa8a5049a6f7b50"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
