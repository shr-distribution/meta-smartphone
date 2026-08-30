require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mindphone"

PV = "20260827-1"

# 32-bit GSI built locally from /media/herrie/HaliumDisk/16.0 with the new
# lineage_halium_arm product (board/generic 32-bit, v30 VNDK snapshot ported
# in from the 14.0 tree + a com.android.vndk.v30 apex_vndk declaration so the
# Android 11 vendor is served). The device runs a 32-bit kernel and 32-bit
# vendor; the published halium_arm64 image cannot work. Switch to a
# https://github.com/webOS-ports/halium-images release once published.
SRC_URI = "file:///media/herrie/HaliumDisk/16.0/halium-luneos-16.0-20260827-1-halium_arm.tar.bz2"
SRC_URI[sha256sum] = "32da7606729cf101eed711762286a0c39438c4d9d3a9c9edaa2fa896ac972e6c"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
