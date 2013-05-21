require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "grouper"

CODENAME = "grouper"

SRC_URI = "http://build.webos-ports.org/phablet/${CODENAME}/phablet-${CODENAME}-${PV}.tar.bz2"
SRC_URI[md5sum] = "f8b1bcc6593bdd60883acfc3d5e1c4cb"
SRC_URI[sha256sum] = "e44fb2fe5c25660fdd1813dc403a8ddbf71f39689bdae60e43eadad015bbf26e"
