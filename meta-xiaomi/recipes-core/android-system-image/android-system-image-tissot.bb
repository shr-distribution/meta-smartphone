require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210506-2"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "4ce6e785a3da6df9708c89714b117e39440df03e706477d837b9ba26cc1bfcc7"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
