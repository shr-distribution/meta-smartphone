require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

# A newer 32-bit GSI than android-system-image-legacy-gsi.inc's, as the TouchPad has its own: 20261008-3 lets
# the sphal namespace link libbinder.so and libui.so for a VNDK 30 vendor too (gralloc.msm8916.so, else no
# UI), and gives minimediaservice five HIDL threads (else a camera picture deadlocks). Published in the
# halium-luneos-20261005 release, which the version's date does not name.
HALIUM_LUNEOS_GSI16_PV = "20261008-3"
HALIUM_LUNEOS_GSI16_SHA256 = "7795e2d7756707ea28a4dfc3d4f00406afe47339abe306168bc0035d2e6712c3"
HALIUM_LUNEOS_GSI16_RELEASE = "halium-luneos-20261005"

COMPATIBLE_MACHINE = "^a3-2015-halium$"

# The 32-bit 16.0 GSI (pinned above, newer than android-system-image-legacy-gsi.inc's), as it is
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
# which the vendor's services link. 20261008-1 adds the camera provider service
# (android.hardware.camera.provider@2.4-service, built in the same tree). Published in the
# halium-luneos-20261005 release of webOS-ports/halium-images.
A3_2015_HALIUM_DEVICE_TARBALL ?= "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-20261005/halium-luneos-11.0-20261008-1-a3-2015-halium.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${A3_2015_HALIUM_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "a95d0f7d5dfde84e3a020f9b2651df6591cdba60ea4f2d8e6b2b7ed7545be181"

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

# This vendor already declares the composer and allocator hwbinder. The camera provider it declares
# passthrough (legacy/0), for LineageOS' cameraserver to load in-process, which the GSI's cannot; the
# device tarball since 20261008-1 carries the provider service (android.hardware.camera.provider@2.4-
# service, built in the same Halium 11 tree as this vendor), and the manifest has to call it hwbinder.
HALIUM_LEGACY_BINDERIZED_HALS = "android.hardware.camera.provider"

# The composer HAL (hwcomposer.msm8916.so) links libhwui.so and libmedia.so directly, and the converter
# puts their Android 11 framework copies in /vendor/lib. libhwui.so needs libft2.so, which the GSI's
# LLNDK list names, so the converter counts it as provided; the vendor linker of the container does not
# hand it to this vendor, though ("library libft2.so not found: needed by /vendor/lib/libhwui.so"), the
# composer exits at once, and init restarts it for ever (8 Oct 2026). A private copy from the old image,
# as for sm-t520 and tenderloin-halium.
HALIUM_LEGACY_EXTRA_LIBS = "libft2.so"

# Those framework copies (libmedia and the rest the composer and camera HALs pull in) want symbols of the
# full libbinder and libmedia that the VNDK 30 snapshot of the GSI does not have: with libft2.so in place
# the composer stops at "cannot locate symbol _ZN7android12MetaDataBase13writeToParcelERNS_6ParcelE"
# (8 Oct 2026). halium-legacy-shim provides them; the converter adds it as a dependency of these
# libraries, the same list as sm-t520's.
HALIUM_LEGACY_SHIM_TARGETS = "libgui.so libmedia.so libmediautils.so libsensor.so libandroid_runtime.so libmedia_codeclist.so"

# Bluetooth: the HAL (android.hardware.bluetooth@1.0-impl) aborts in initialize() with "Open: No Bluetooth
# Address!": it reads the address from the file ro.bt.bdaddr_path names, which LineageOS sets in
# msm8916-common's system.prop (/efs/bluetooth/bt_addr), and nothing mounted /efs (8 Oct 2026). Carry the
# Bluetooth properties of that system.prop into the vendor build.prop and mount EFS, read-only, through an
# fstab of its own (the vendor's fstab.qcom has no /efs line). EFS also holds the Wi-Fi MAC that
# wcnss_service hands the firmware (/efs/wifi/.mac.info).
HALIUM_LEGACY_SYSTEM_PROP_PREFIXES += "ro.bt. ro.bluetooth. ro.qualcomm.bt. vendor.bluetooth. vendor.qcom.bluetooth."
SRC_URI += "file://fstab.efs file://zz-a3-bluetooth-efs.rc"
HALIUM_LEGACY_EXTRA_FILES += "fstab.efs:/etc/fstab.efs:644"
# The address file is radio:net_bt_stack 0640, which the HAL (user and group bluetooth) cannot read; the
# rc overrides the vendor's service definition with group net_bt_stack added.
HALIUM_LEGACY_EXTRA_FILES += "zz-a3-bluetooth-efs.rc:/etc/init/zz-a3-bluetooth-efs.rc:644"

# The camera daemon and the camera provider need a target SDK below 23 to load the vendor's camera libraries
# that have text relocations; see zz-a3-camera-textrel.rc. The shim that does it is the shared one of the
# Android 9 services (android-system-image-legacy-a9-services.inc), which the TouchPad's and the Nexus 5's
# camera providers already load; only that file is taken from the set.
require recipes-core/android-system-image/android-system-image-legacy-a9-shim.inc
SRC_URI += "file://zz-a3-camera-textrel.rc"
HALIUM_LEGACY_EXTRA_FILES += "zz-a3-camera-textrel.rc:/etc/init/zz-a3-camera-textrel.rc:644"
