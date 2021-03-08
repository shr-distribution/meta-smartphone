require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "hammerhead"

PV = "20210306-1"

#SRC_URI = "http://build.webos-ports.org/halium-luneos-5.1/halium-luneos-5.1-${PV}-${MACHINE}.tar.bz2"
SRC_URI = "file:///media/LuneOS/Android/Halium-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "ad0eaf28deaff17b8c041936654f2a1d"
SRC_URI[sha256sum] = "dce5f8e6dc148568845a9310b7c3e8bae2904ceb075a4ea486341f9411a9b3f8"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
