require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20200812-43"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "13b764fe0057fdd50abf4bb87b855676"
SRC_URI[sha256sum] = "4dc8926e2bafd0ff75c460c695df9ed32a0d010b98967db6322791ac39406616"
