require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20180914-22"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "51bb7f9b259d1abbe54693685ed5358b"
SRC_URI[sha256sum] = "f8d1e205e5ae518b46c7f6cf72c516aaa946579463be28ef855ef41f3d9c8cdd"
