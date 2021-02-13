require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210213-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "5876d67932eaa5a38755c14940f56126"
SRC_URI[sha256sum] = "8fe2347db0beaa5644c81d61f9e1aa24d47aa9e5e6c73612b7757179fd5098d1"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
