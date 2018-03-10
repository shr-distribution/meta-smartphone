require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20180308-9"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "62919c2f334566345b66b404913758ad"
SRC_URI[sha256sum] = "99750a788483299ed74d53de3358c853a6626bc66621badf31a37256afecdbad"
