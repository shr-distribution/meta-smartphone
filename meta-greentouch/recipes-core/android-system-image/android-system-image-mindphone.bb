require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "mindphone"

# Pinned, not shared: the 32-bit GSI is a separate build on its own cadence, and
# the release the arm64 ports track (20260910) published no halium_arm asset.
# Everything else comes from halium-luneos-gsi-16.inc, so the URL is still
# assembled in exactly one place. The same build as the legacy 32-bit devices
# (android-system-image-legacy-gsi.inc); it carries com.android.vndk.v27 to v34.
HALIUM_LUNEOS_GSI16_ARCH = "halium_arm"
HALIUM_LUNEOS_GSI16_PV = "20261007-1"
HALIUM_LUNEOS_GSI16_SHA256 = "3f62631989cc03148725e22be84d3c60ed68f16cc8f5aef56157ee612fae5ae4"

PV = "${HALIUM_LUNEOS_GSI16_PV}"

# 32-bit GSI: the device runs a 32-bit kernel and a 32-bit vendor, so the
# published halium_arm64 image cannot work. Built with the lineage_halium_arm
# product (board/generic 32-bit, v30 VNDK snapshot ported in from the 14.0 tree
# plus a com.android.vndk.v30 apex_vndk declaration so the Android 11 vendor is
# served).
#
# The release is tagged by date and holds every generation's tarball, so the tag
# and the asset name are not the same string - the asset carries its own
# revision date, which is what PV tracks.
SRC_URI = "${HALIUM_LUNEOS_GSI16_URL}"
SRC_URI[sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
