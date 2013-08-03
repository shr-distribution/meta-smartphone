require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "37983745b5e4a931512c1c042e7b0c62"
SRC_URI[sha256sum] = "0b92a2dca1e833ff8754bf64d110389492e4f74577f3b1c254d07f7c65a63d81"

