require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "tissot"

PV = "20210107-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"

SRC_URI[md5sum] = "be5460225a4a74d1bee447cdd2f8d187"
SRC_URI[sha256sum] = "fdcda1e8ee5341ed30bdeb131430894cf071608753dcec35ee5c457aec24aa84"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"