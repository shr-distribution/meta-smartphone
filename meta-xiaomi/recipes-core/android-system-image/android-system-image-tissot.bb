require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210517-3"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "e9c776d5c60a26ef86ddb4a1c6e91cf0d659f20396be01a2784ebc75825fc377"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
