require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "^rosy$"

PV = "20180802-18"

SRC_URI = "http://build.webos-ports.org/halium-luneos-7.1/halium-luneos-7.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "a30093882f32472bb9ce0b892e7a6be7"
SRC_URI[sha256sum] = "832a7c5c1e94e76b4fcb0e0311283e8c526d9528c9855d55236a54cc6332fb65"
