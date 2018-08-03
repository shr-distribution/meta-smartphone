require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180802-18"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "2840c9ef9aa94afbbb97878afdd3a9d3"
SRC_URI[sha256sum] = "59816d52dfe4b83ecb5e652a260a232f0aae26767510c90885d43a812ce6b90d"
