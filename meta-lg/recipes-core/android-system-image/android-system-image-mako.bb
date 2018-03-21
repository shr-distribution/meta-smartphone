require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20180311-15"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e905f4695c32886745ad32b12663d9ff"
SRC_URI[sha256sum] = "cb09752f09953f49c4619a71f6873f27af57eec1247e68a046a17c3bf38cdbd5"
