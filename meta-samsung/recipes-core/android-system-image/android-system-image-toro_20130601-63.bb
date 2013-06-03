require recipes-core/android-system-image/android-system-image.inc

PR = "${INC_PR}.1"

COMPATIBLE_MACHINE = "toro"

SRC_URI = "http://build.webos-ports.org/phablet/phablet-${MACHINE}-${PV}.tar.bz2"
SRC_URI[md5sum] = "831e1b2e010323c26baac794d0840510"
SRC_URI[sha256sum] = "b9787871d287cd412d9f5425ff233d9f9d51f28402d42cc0839f9cafcd08e81f"
