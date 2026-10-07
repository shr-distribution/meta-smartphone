require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "^sm-t220$"

PV = "${HALIUM_LUNEOS_GSI16_PV}"

# The 64-bit GSI, as radon: the kernel is arm64 and so is the vendor's primary
# ABI (TARGET_ARCH arm64 in Lineage's BoardConfigCommon.mk).
#
# The stock vendor is Android 12 with VNDK 31 (see sm-t220.conf), the same
# generation as mp01, which runs this GSI generation. Not yet booted here.
SRC_URI = "${HALIUM_LUNEOS_GSI16_URL}"
SRC_URI[sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"

# For Android 9+, it's highly recommended to use a rootfs system image
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
