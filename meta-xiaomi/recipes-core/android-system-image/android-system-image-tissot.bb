require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot-halium"

PV = "20210517-3"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-tissot.tar.bz2/halium-luneos-9.0-${PV}-tissot.tar.bz2"
SRC_URI[sha256sum] = "e9c776d5c60a26ef86ddb4a1c6e91cf0d659f20396be01a2784ebc75825fc377"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
