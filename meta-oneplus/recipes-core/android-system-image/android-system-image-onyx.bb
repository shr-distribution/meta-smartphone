require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20180303-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "95799b41a85c3f65f2191ce8d041c78a"
SRC_URI[sha256sum] = "c3f9a92809e3a3dac0b65c7eaa830aaf86a861e1c73dc70f46a0fd1f823f398e"
