require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "tuna"

CODENAME = "maguro"
BUILDVERSION = "20130518-38"

SRC_URI = "http://build.webos-ports.org/phablet/maguro/phablet-${CODENAME}-${BUILDVERSION}.tar.bz2"
SRC_URI[md5sum] = "fb465681184d26a8ecfa869c23e2642d"
SRC_URI[sha256sum] = "d8b6bce5806732a52b59bf045429b66f51ea4e20a7b5b805d618a7e525ebed3a"
