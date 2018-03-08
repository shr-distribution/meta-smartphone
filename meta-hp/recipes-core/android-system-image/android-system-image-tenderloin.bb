require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20180308-14"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7ef8551d503e11c183e585e7ac5c624d"
SRC_URI[sha256sum] = "ea04e4214949578814785d4c976e657dcdf11ad2b922093b49aee1fc6bdb3fb9"
