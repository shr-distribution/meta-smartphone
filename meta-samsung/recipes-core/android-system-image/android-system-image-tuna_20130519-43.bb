require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "tuna"

CODENAME = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/${CODENAME}/phablet-${CODENAME}-${PV}.tar.bz2"
SRC_URI[md5sum] = "f519478c3c68d4f6ebeecfd96a577c15"
SRC_URI[sha256sum] = "1f67fd350a55b3121066c7c5e091a833a5cc9a2713c9ff5ead3b6b276f8bfb3f"
