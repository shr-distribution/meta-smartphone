require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mido-halium"

PV = "20210506-2"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-$mido.tar.bz2/halium-luneos-9.0-${PV}-mido.tar.bz2"
SRC_URI[sha256sum] = "af9deead686663ceab1717fe3d76ade19207e91191761eb23778bee7107de994"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
