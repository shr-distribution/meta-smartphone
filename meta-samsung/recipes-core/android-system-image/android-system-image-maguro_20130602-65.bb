require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "4990f3a84a73850312db9d266d8166ca"
SRC_URI[sha256sum] = "de002c106ba0beaa2f369eb31785f6337a50bc0ccce6e7a15453b7062d41ce0c"
