require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "maguro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "a5303c0f7595ae65a03ba9405b230aab"
SRC_URI[sha256sum] = "05f93dd9c98278d5ef62d71b55af00543be129fbdacb93faa77430cad505156e"

