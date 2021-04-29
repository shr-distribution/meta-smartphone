require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido"

PV = "20210428-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "a05798bd7ebfa73f836db7cafc765985"
SRC_URI[sha256sum] = "db91356b769f49d3c0d097f32ac555b30eb0fd4292ec7b254f52fe1ca86c5dda"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
