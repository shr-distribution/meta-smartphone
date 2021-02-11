require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "s2"

PV = "20210204-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "b5f2b8bbfcde7bd02da1a806669f397e"
SRC_URI[sha256sum] = "1292e32fc6e036254757a25901f3b8b9d5a1dfb13cf336ce61d0df95d2d62aef"


