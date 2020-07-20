require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20200711-42"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "a21650cdc0373e1d6428ef733149422b"
SRC_URI[sha256sum] = "a00b3c8fd4c0981c0fef5d53bdce51d7467c4569cec73324ab61a2238620be73"
