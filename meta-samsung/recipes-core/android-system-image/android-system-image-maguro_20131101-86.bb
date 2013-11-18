require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "b5aa9df192b683fdfc3d8a55ae97a17d"
SRC_URI[sha256sum] = "83127100129d4598a6fca9f00bd3ff23dadbafc702ba72ad9408439f7182543f"

