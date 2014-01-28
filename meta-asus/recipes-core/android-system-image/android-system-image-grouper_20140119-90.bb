require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "8f959280553d504cdc3d50bfc4eb13d8"
SRC_URI[sha256sum] = "775234b3a6d6759abc4577185ee4a96340979375cd99d20206221aee3233bcc4"
