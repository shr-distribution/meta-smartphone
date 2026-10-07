require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

COMPATIBLE_MACHINE = "^a3-2015-halium$"

# The 32-bit 16.0 GSI (HALIUM_LUNEOS_GSI16_* in android-system-image-legacy-gsi.inc), as it is
# published, on a vendor that comes from vlw's LineageOS 18.1 trees for the Galaxy A3 (2015)
# (github.com/vlw: android_device_samsung_a3lte, android_device_samsung_msm8916-common,
# proprietary_vendor_samsung, branch lineage-18.1).
#
# That build is Android 11 (VNDK 30) and has no vendor partition: it puts the vendor under
# /system/vendor (PRODUCT_VENDOR_MOVE_ENABLED, no BOARD_VENDORIMAGE_*), so it goes through the same
# conversion as sm-t520 (halium-legacy-vendor), with no init scripts to move: the Android 11 vendor
# ships its init files in /vendor/etc/init already. The GSI carries com.android.vndk.v30 for 32-bit
# ARM (PRODUCT_EXTRA_VNDK_VERSIONS 27..34 in device/halium/halium_arm).
HALIUM_LEGACY_VNDK = "30"

# The "device" tarball is the system.img of that build (lunch lineage_a3lte-userdebug in the
# Halium 11 tree, packed with "m snod" after "m -k systemimage", because the ART apex check fails for
# this 32-bit product and stops the full target; the ART apex does not matter for a vendor). It is a
# system-as-root image whose root holds system/, with the vendor under system/vendor and the build's
# own ownership and modes, which is the layout legacy_vendor.py reads. Its /system/lib is completed the
# way sm-t520's is: the real Android 11 libandroidicu, libicuuc, libicui18n, libnativehelper (ART apex),
# libstatspull and libstatssocket (statsd apex) in place of the links into /apex, which the framework
# copies under the camera HAL (libandroid_runtime, libhwui, libharfbuzz_ng) need, and the HIDL
# interface libraries of /system/system_ext/lib (vendor.lineage.livedisplay@2.0, touch, trust, power),
# which the vendor's services link. Not published anywhere yet, so it is a local file.
# TODO: publish to webOS-ports/halium-images and use an https URL.
A3_2015_HALIUM_DEVICE_TARBALL ?= "file:///home/herrie/claude-scratch/a3-2015-halium/out/halium-luneos-11.0-20261006-2-a3-2015-halium.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${A3_2015_HALIUM_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "a74afdf6edcf894ce937a799042429957c2cf6d31d37bc22cc3f72760fd8ae32"

# No device init scripts to move (see above).
HALIUM_LEGACY_INIT_FILES = ""

# The plain GSI: nothing of the old image goes into it. The vendor's fstab (fstab.qcom) mounts
# apnhlos on /firmware, modem on /firmware-modem and hidden on /hidden, root directories the old image
# had (BOARD_ROOT_EXTRA_FOLDERS in msm8916-common) and the GSI does not, so legacy_vendor.py moves
# them into the vendor image (/vendor/firmware_mnt, /vendor/firmware-modem, /vendor/hidden) and points
# the firmware links of /system/etc/firmware (modem.mdt and the rest) there from /vendor/firmware.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# What LineageOS' linker does for TARGET_LD_SHIM_LIBS in msm8916-common's BoardConfigCommon.mk
# (/system/vendor/lib/hw/camera.vendor.msm8916.so|libshim_camera.so and
# /system/vendor/lib/libperipheral_client.so|libshim_binder.so): the Android 16 linker has no such
# feature, so the shim becomes a dependency of the library. Both shims are in /vendor/lib.
# (The same list's /system/bin/mediaserver|libstagefright_shim.so is for a system binary the GSI
# replaces.)
HALIUM_LEGACY_LD_SHIMS = "camera.vendor.msm8916.so|libshim_camera.so libperipheral_client.so|libshim_binder.so"

# This vendor already declares the composer and allocator hwbinder, so there is nothing to rewrite
# in its manifest.
HALIUM_LEGACY_BINDERIZED_HALS = ""
