require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20180311-15"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "250c611827c4c63d35d173f001b371ff"
SRC_URI[sha256sum] = "df77358b9361f736beebcbb15c53353480a565bdaa90cdb3a1052b913c12fe45"
