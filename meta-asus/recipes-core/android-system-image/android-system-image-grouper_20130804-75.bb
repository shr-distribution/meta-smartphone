require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "e4a90948c597e61e2aac9be7906d3e61"
SRC_URI[sha256sum] = "433e6c93e0d401f06504892050dc839c85f60ef9534478ebc9bfe9114411ba67"
