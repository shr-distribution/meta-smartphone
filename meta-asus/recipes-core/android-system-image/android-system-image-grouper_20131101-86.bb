require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "6ace1488d491f7ed884fa878f504d1f6"
SRC_URI[sha256sum] = "8d8e9803ad53c5df19869dc518358963501aa7d50953fe58c0ccba400f8ad7dc"
