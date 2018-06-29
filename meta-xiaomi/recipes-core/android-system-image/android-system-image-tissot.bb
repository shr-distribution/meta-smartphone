require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20180628-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "998ed9bfbf2e4d5eff0ce0f00725db27"
SRC_URI[sha256sum] = "bcf6d729c1d706780e790d26765a6932f93bbf1309939fba80b0e211650c7cae"
