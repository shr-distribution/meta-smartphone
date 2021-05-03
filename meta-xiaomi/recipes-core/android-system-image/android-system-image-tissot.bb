require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210429-18"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "e2a6b12e42306c73afe83a144ca89c749b7adf18bbe086bafb8f016f98368eee"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
