require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20180311-10"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "c44e4cefb875fea36fb4748b43cad93b"
SRC_URI[sha256sum] = "19bff2d7034a5f233ab019184424645bc5acac066d620c49d76596888e261231"
