require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sargo"

PV = "20240301-3"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "be2f6d8d93b26ae4669c75f13c5783f6f51a480bc4275d549898582adf3bfb41"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
