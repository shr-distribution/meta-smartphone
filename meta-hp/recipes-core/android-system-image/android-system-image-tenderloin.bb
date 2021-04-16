require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tenderloin"

PV = "20210416-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "aaf136a0ad268aa2de9b0bc6c0a6336b"
SRC_URI[sha256sum] = "257f5d1db3e048ef7f30833315a906ba26f767e643b6c3e49c3ccae706dfd1df"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
