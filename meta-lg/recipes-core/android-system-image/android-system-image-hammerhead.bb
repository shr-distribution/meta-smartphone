require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20180308-14"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "e40192735a9d222aa09eb86aa7f1a542"
SRC_URI[sha256sum] = "411f5734666564f77df4347ceb7401c90ee2cd1e8af0703fc5ba15de7fad751e"
