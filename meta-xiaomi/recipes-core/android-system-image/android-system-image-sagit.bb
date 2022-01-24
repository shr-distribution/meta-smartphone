require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sagit"

PV = "20210202-herrie"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "552628e0944a8e677b886278a38fac3f60f1ca62a9358d61dba3a546eb1e87da"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
