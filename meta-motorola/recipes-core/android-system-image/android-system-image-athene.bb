require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athene"

PV = "20180311-10"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "9005c345b63dabc1739530c890f2df7b"
SRC_URI[sha256sum] = "afc8fcacfdb88a72bb1d92792209d5aec0e6a4fc1fd43b1e90a7f3e6c000b3ac"
