require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mako"

PV = "20210422-1"

SRC_URI = "http://build.webos-ports.org/halium-luneos-9.0/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[md5sum] = "dfed2ba6e6c094ffbbae03b1bf343cc3"
SRC_URI[sha256sum] = "d0b214e7372f6c67b08e9e4416f57a32e8241606c4f1a543754622d7a8f0d166"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
