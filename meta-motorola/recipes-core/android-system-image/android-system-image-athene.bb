require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athene"

PV = "20180308-9"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "7bc77698d857425c2cf6a5c8b73d75c7"
SRC_URI[sha256sum] = "fcde7c640f6a64ef0f57171797909105a022971ff3cdac3f76a173984c8c2094"
