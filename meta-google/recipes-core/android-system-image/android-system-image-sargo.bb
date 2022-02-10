require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sargo"

PV = "20220205-1"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
SRC_URI[sha256sum] = "0a0b2f396b39018cd53a3c0fc2fb273623a21fc7ece40be019f43f59af7f8937"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
