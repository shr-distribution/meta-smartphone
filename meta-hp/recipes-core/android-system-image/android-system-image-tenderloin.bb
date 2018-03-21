require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20180311-15"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "475ad34433d7a6e66db3084c9e1587e9"
SRC_URI[sha256sum] = "ba67f4e6b38dacd12e62d5dbda75831828d2d6ebf61d2d7695a57e4493a06d9c"
