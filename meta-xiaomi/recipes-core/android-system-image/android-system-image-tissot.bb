require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210214-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "a2d43e4426873577c72cc258baa619e8"
SRC_URI[sha256sum] = "8febc2a825be6d7b49437b77c84433f6f28fff3b994ba06248b6ee8955cb302f"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
