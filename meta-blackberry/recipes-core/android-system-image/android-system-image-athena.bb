require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "athena"

PV = "${HALIUM_LUNEOS_GSI16_PV}"

# 64-bit GSI. athena inherits core_64_bit.mk in the LineageOS device tree and
# the vendor is arm64, so the stock halium_arm64 product is right as-is - none
# of the 32-bit-primary work mindphone needed, and none of the VNDK work either:
# the vendor this runs against is Android 15, which has no VNDK at all (no
# ro.vndk.version, no VNDK payload, an all-HIDL VINTF manifest), so the 16.0
# GSI serves it without a ported snapshot.
#
# NOTE: confirm the release tag before trusting this URL. The sha256 below is
# of the local copy at
#   /media/herrie/HaliumDisk/gsi-20260826/halium-luneos-16.0-20260826-1-halium_arm64.tar.bz2
# which is what the flash kit was built from; the published asset under some
# halium-luneos-<date> tag should be byte-identical, but that has not been
# checked against the network here.
# Release, tarball and checksum come from halium-luneos-gsi-16.inc so every
# arm64 port moves together - see the header there for what the drift cost.
SRC_URI = "${HALIUM_LUNEOS_GSI16_URL}"
SRC_URI[sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
