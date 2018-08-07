require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180807-19"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "fd6bde30bcaad219a1951179e9c6216e"
SRC_URI[sha256sum] = "a33ff250bcfc09f75f62b5e4c6cf8e7681defb040f2d61e763bc6d0efe1416f0"
