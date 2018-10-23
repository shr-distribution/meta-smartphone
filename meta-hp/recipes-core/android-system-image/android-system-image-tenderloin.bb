require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20181023-19"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e2de944384df39812406aae297bd8bcb"
SRC_URI[sha256sum] = "2f2211fb59d412df249d91e61a45358fe4a5c9e6684155cb817450ca90da38c6"
