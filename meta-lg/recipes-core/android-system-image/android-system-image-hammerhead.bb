require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

# PV went backwards due to loss of image on file server. Using previous build from backup.
# PV = "20210506-4"

PV = "20210506-3"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
# SRC_URI[sha256sum] = "c4f285da5c8239d836d518519848b094230de426055c339cabd8b139adeb6223"
SRC_URI[sha256sum] = "83d3e7388a5e65c33c48e6104a38449c349bffbc486db1b378850baa2f4fdfb8"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
