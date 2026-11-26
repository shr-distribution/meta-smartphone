require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "mindphone"

# The version every device shares (halium-luneos-gsi-16.inc; until 20261116-1 the
# 32-bit GSI was pinned here, on a cadence of its own). The same build as the legacy 32-bit devices
# (android-system-image-legacy-gsi.inc); it carries com.android.vndk.v27 to v34.
# 20261017-1 adds droidmedia's guard against releasing a codec that was never
# created: with no media.resource_manager under Halium on Android 16, MediaCodec
# refuses every codec, and every AAC file then crashed the player in
# MediaCodec::release().
# 20261104-1 adds droidmedia's decoder fixes (see halium-luneos-gsi-16.inc) and
# the Widevine linkerconfig link, which has nothing to load here: the bridge is
# 64-bit only.
# 20261107-1 changes only droidmedia (libdroidmedia.so, minimediaservice) and
# build.prop's dates against 20261104-1.
HALIUM_LUNEOS_GSI16_ARCH = "halium_arm"

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
