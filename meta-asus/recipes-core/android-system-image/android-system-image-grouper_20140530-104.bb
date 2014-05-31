require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "3bc7e684ae9244f8554d0c393e884b81"
SRC_URI[sha256sum] = "cc403f85d3e181c013abd78eaec1b0b3d3643e268689598de0755a2e36a9dee1"
