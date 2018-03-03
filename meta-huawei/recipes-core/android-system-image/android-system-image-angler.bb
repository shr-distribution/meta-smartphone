require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "angler"

PV = "20180303-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "c3dfb7c81d6c9c5d39dfe92a9acd2fa1"
SRC_URI[sha256sum] = "ace79f7f8914bacefb5e6ba4e379ca604aa110a78758cf79515a45bb526d1b86"

