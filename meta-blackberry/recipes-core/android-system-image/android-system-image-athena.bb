require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "athena"

PV = "20260826-1"

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
SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-20260826/halium-luneos-16.0-${PV}-halium_arm64.tar.bz2"
SRC_URI[sha256sum] = "8bcbb68b0bab9870d19560931a29c36e484873c797a32302048900e16cc147f3"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
