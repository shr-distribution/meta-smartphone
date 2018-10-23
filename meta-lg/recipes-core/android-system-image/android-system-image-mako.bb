require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20181023-19"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "f5056f6807e9df8faf5682fb467be30b"
SRC_URI[sha256sum] = "3f3a6eaa56fdd138192cf30545b7c0cd6e720b4c4f49bd11d095be5713f85bb6"
