require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athene"

PV = "20180914-22"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "17c4855df6b8e46621af7ca03d3ca0a6"
SRC_URI[sha256sum] = "c99cdb4363f9d74611de3a16e5ce8ad45962a1a15a894cd56b907ec9d7585278"
