require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead-halium"

PV = "20230127-1"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-hammerhead.tar.bz2/halium-luneos-9.0-${PV}-hammerhead.tar.bz2"
SRC_URI[sha256sum] = "a3b058dd2bafd6d9af661b03f54cb35e115dd5601624534e34a271bfde1b1e46"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
