require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sagit"

PV = "20210202-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "7da9926a1a90fbedcb8f96b6f8c8b7df"
SRC_URI[sha256sum] = "552628e0944a8e677b886278a38fac3f60f1ca62a9358d61dba3a546eb1e87da"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
