require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

COMPATIBLE_MACHINE = "^vince-halium$"

# The arm64 GSI, not the 32-bit one android-system-image-legacy-gsi.inc picks: the vendor
# is arm64 with a 32-bit second architecture (the camera HAL is 32-bit only:
# BOARD_QTI_CAMERA_32BIT_ONLY). It carries VNDK 30, which is what this vendor needs.
HALIUM_LUNEOS_GSI16_ARCH = "halium_arm64"
HALIUM_LUNEOS_GSI16_PV = "20261017-1"
HALIUM_LUNEOS_GSI16_SHA256 = "dc789b56a4709ae3081473fc5e897aa3bf9d919b73e12ad7c64288386104dd45"

# The vendor image published with the GSI (vince-halium-vendor.img in its release).
#
# Nothing is derived. Unlike tissot and mido, whose Android 9 vendors predate Treble, and
# unlike a3-2015-halium, whose LineageOS 18.1 build has its vendor under /system, vince's
# LineageOS 18.1 build has a real vendor image: BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE,
# TARGET_COPY_OUT_VENDOR := vendor and BOARD_VNDK_VERSION := current. It is built with
#
#   lunch lineage_vince-userdebug && m vendorimage
#
# ("m -k vendorimage" then "m vnod": the ART APEX check fails in this tree, which disables
# ART, and it does not matter for a vendor) in the Halium 11 tree (LineageOS 18.1) from
# LineageOS's android_device_xiaomi_vince and android_device_xiaomi_msm8953-common, branch
# lineage-18.1, the msm8953 maintainers' temp_android_kernel_xiaomi_vince, and the blobs of
# the-muppets/proprietary_vendor_xiaomi (lineage-18.1, 5ef13fcc). vendor/samsung and device/samsung are hidden from soong for
# that build: a3lte's global 32-bit prebuilts break an arm64 product. The image is the
# sparse build output unpacked and shrunk to its contents (resize2fs -M).
HALIUM_LEGACY_VENDOR_SHA256 = "e568132fc048ae7a93c323cab3fe276a05ec196e81464970ad2515cab404c5ea"

# android-system-image-legacy-gsi.inc wants these for deriving, which this recipe never does.
HALIUM_LEGACY_DEVICE_URL = ""
HALIUM_LEGACY_DEVICE_SHA256 = ""
HALIUM_LEGACY_INIT_FILES = ""
HALIUM_LEGACY_VNDK = "30"

# The plain GSI: a published vendor image goes with it unchanged.
HALIUM_LEGACY_GSI_AUTODETECT = "0"
