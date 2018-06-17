require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20180610-1"

SRC_URI = "file:///media/LuneOS/Android/Halium-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "a0bffdfcc62174d248f3df4a6633f779"
SRC_URI[sha256sum] = "0cb5729ea4b82fffa32893c09e9dc11e7db13c9afbd2cc8dc932730bdc7d3230"
