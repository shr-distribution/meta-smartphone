require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20171130-5"

SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "a19ade34eb59d210cd0283c692aa4043"
SRC_URI[sha256sum] = "0bdec9a54e8b101dda3ed6a30bd1c55aef35cbae6af462446978ca006b63187b"
