require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "6b819129402b03cb5b9006f7a99f60d1"
SRC_URI[sha256sum] = "8d7a71b81103a8a07c0fab9c50c920ccd63c06b6c5b679b1e78f1b7db9a9c48c"

