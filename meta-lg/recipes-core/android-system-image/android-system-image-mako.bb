require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20180308-14"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "dd768ba780b5e1b19e98adfa754d891c"
SRC_URI[sha256sum] = "00a693e196e7d66aa6aa2fb552a7b4a97a0695d6be00ba0a6604f04950567cf5"
