require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

# PV went backwards due to loss of image on file server. Using previous build from backup.
# PV = "20210506-2"

PV = "20210420-2"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
# SRC_URI[sha256sum] = "d6bb53f9e886428faeb589c61b98b5e0345789b6587cb0b01a3e34cb546f92fb"
SRC_URI[sha256sum] = "9bf006c6dd16d37da34de665885b30aaca6accbf104405db156372f09ae66371"
 
# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
