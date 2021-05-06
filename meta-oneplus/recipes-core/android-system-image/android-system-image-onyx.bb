require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "onyx"

PV = "20200812-43"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "6404ec7a432491e9c603cc3fc858be11"
SRC_URI[sha256sum] = "f24559a72d7d75cf9a066c83e2a69d20cacded37e1c7c343918680c2fd00ddf2"
