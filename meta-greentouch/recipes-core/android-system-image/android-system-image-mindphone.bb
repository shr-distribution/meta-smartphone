require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mindphone"

PV = "20260827-1"

# 32-bit GSI: the device runs a 32-bit kernel and a 32-bit vendor, so the
# published halium_arm64 image cannot work. Built with the lineage_halium_arm
# product (board/generic 32-bit, v30 VNDK snapshot ported in from the 14.0 tree
# plus a com.android.vndk.v30 apex_vndk declaration so the Android 11 vendor is
# served).
#
# The release is tagged by date and holds every generation's tarball, so the tag
# and the asset name are not the same string - the asset carries its own
# revision date, which is what PV tracks.
SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-20260826/halium-luneos-16.0-${PV}-halium_arm.tar.bz2"
SRC_URI[sha256sum] = "32da7606729cf101eed711762286a0c39438c4d9d3a9c9edaa2fa896ac972e6c"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
