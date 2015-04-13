require recipes-core/android-system-image/android-system-image.inc

PV = "20150323-117"
PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

SRC_URI = "http://build.webos-ports.org/cm-wop-10.1/cm-wop-10.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "3490c9c7e0092e3bca77da349e9f1c67"
SRC_URI[sha256sum] = "1adcb673a244b174ebc6fad33b821a251330cbf173504d8078e14b1cff0e6d8a"
