require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "62aed0551efffff7e449a803e7759e48"
SRC_URI[sha256sum] = "6a9d1824347e14ecbfe46328bc0c161d47880f1d86c62a5f78539346151d0f09"
