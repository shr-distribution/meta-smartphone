require recipes-core/android-system-image/android-system-image.inc
require recipes-core/android-system-image/halium-luneos-gsi-16.inc

COMPATIBLE_MACHINE = "radon"

PV = "${HALIUM_LUNEOS_GSI16_PV}"

# 64-bit GSI, and no VNDK work of any kind is needed here.
#
# The vendor this runs against is Android 12L - FuriLabs' own vendor build,
# whose packaging says so in its name (radon-vendor-32) and whose adaptation
# metapackage depends on adaptation-hybris-api32-phone. That means VNDK 32.
#
# The stock halium_arm64 16.0 GSI already ships prebuilts/vndk v31 through
# v34, so com.android.vndk.v32 is present as built and an api32 vendor is
# served directly. This is the easy case: mindphone needed a whole v30 snapshot
# ported into the 16.0 tree because 16 ships nothing below v31, and athena got
# away with an Android 15 vendor that has no VNDK at all. radon lands neatly
# inside the range the tree already covers.
#
# Release, tarball and checksum come from halium-luneos-gsi-16.inc so every
# arm64 port moves together - see the header there for what the drift cost.
SRC_URI = "${HALIUM_LUNEOS_GSI16_URL}"
SRC_URI[sha256sum] = "${HALIUM_LUNEOS_GSI16_SHA256}"

# For Android 9+, it's highly recommended to use a rootfs system image.
#
# On this device that is not merely a recommendation but the only sane choice:
# the FLX1s has dynamic partitions, so "the system partition" is a dm device
# carved out of super at runtime. Shipping the GSI as a file inside our own
# rootfs and loop-mounting it means super is never written at all - which
# matters twice here. FuriLabs' own vendor image lives in that same super and
# we want it left alone; and this device is flashed with SP Flash Tool rather
# than fastboot, so there is no fastbootd to flash a GSI to a dynamic
# partition with in the first place.
ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
