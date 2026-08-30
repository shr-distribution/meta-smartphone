require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "mindphone"

PV = "20260830-1"

# 32-bit GSI: the device runs a 32-bit kernel and a 32-bit vendor, so the
# published halium_arm64 image cannot work. Built with the lineage_halium_arm
# product (board/generic 32-bit, v30 VNDK snapshot ported in from the 14.0 tree
# plus a com.android.vndk.v30 apex_vndk declaration so the Android 11 vendor is
# served).
#
# The release is tagged by date and holds every generation's tarball, so the tag
# and the asset name are not the same string - the asset carries its own
# revision date, which is what PV tracks.
SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-20260830/halium-luneos-16.0-${PV}-halium_arm.tar.bz2"
SRC_URI[sha256sum] = "77d6ce81311d5e57939d961d196a0908c140db6403a54e8be180c0e4bb29eb82"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
