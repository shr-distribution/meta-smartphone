require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mindphone"

PV = "20240228-1"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-11.0-20240219-1-halium_arm64.tar.bz2/halium-luneos-11.0-20240219-1-halium_arm64.tar.bz2"
SRC_URI[sha256sum] = "355805c5bca803a386065c30408cfffb6024c7dfc34768a0ba61039747fe3976"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
