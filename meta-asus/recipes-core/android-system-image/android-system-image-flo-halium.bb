require recipes-core/android-system-image/android-system-image-legacy-gsi.inc
# The camera provider below preloads the permission-cache shim.
require recipes-core/android-system-image/android-system-image-legacy-a9-shim.inc

COMPATIBLE_MACHINE = "^flo-halium$"

# The 32-bit 16.0 GSI (HALIUM_LUNEOS_GSI16_* in android-system-image-legacy-gsi.inc) on the
# vendor of the LineageOS 18.1 build for the Nexus 7 (2013) Wi-Fi, flox, as published by its
# maintainer (github.com/ods-releases/flox, lineage-18.1-20260923-UNOFFICIAL-flox.zip). Nothing
# of it is built here: that zip's system and vendor images are the input.
#
# That build is Android 11 (VNDK 30) with a vendor partition of its own, but the vendor is not
# self-contained: its graphics and keymaster HALs link libqdutils, libmemalloc, libstdc++,
# libkeymaster3device and the vendor.lineage.* interfaces from the Lineage /system/lib. So it goes
# through halium-legacy-vendor like sm-t520's, which pulls those in, with VNDK 30 instead of 28.
# The GSI carries com.android.vndk.v30 for 32-bit ARM.
HALIUM_LEGACY_VNDK = "30"

# The vendor image published with the GSI (flo-halium-vendor.img in its release), made by the settings
# below against that GSI. Not published yet, so it is derived at build time from the device tarball.
# TODO: publish UNPACKDIR/vendor.img to webOS-ports/halium-images and set its sha256 here.
HALIUM_LEGACY_VENDOR_SHA256 = ""

# The "device" tarball is a system.img laid out like a monolithic Halium build: a root holding
# system/ (bin, lib, etc, build.prop, the system_ext libraries and the ICU, nativehelper and statsd
# libraries out of their APEXes) with the vendor image's tree under system/vendor, and the device
# init scripts at the root. assemble-old-system.sh (kept with the port's scratch files, not in this
# layer) makes it from the OTA zip above, the same way sm-t520's is made from its build. Not
# published anywhere yet, so it is a local file.
# TODO: publish to webOS-ports/halium-images and use an https URL.
FLO_DEVICE_TARBALL ?= "file:///home/herrie/claude-scratch/flox/out/halium-luneos-11.0-20261015-1-flo.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${FLO_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "7ad864610c78ec373f80df9c3c99496641540fa790d207add6f664b6e501f5ed"

# flox keeps its board init scripts at the root of its system-as-root image, not in the vendor:
# init.flox.rc starts what the hardware needs (rmt_storage, qmuxd, mm-qcamera-daemon, the WCNSS
# start-up in init.flox.wifi.sh, devstart_sh, which boots the ADSP for audio), and imports the
# other two. The converter puts them in /vendor/etc/init/hw and drops their mount_all lines.
HALIUM_LEGACY_INIT_FILES = "init.flox.rc init.flox.usb.rc init.flox.power.rc ueventd.flox.rc"

# The GSI is the plain halium_arm one, which has /persist and /firmware itself: nothing of the old
# image goes into it.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# The vendor already declares its composer, allocator and OMX as hwbinder services, built for this
# Android 11 vendor, so those stay. Its camera HAL (camera.flox.so, with mm-qcamera-daemon) has only
# a passthrough provider, which the Android 16 userspace does not load: it gets the Android 9
# provider service of android-system-image-legacy-a9-services.inc, with that set's wait script (the
# daemon is the same Qualcomm mm-qcamera-daemon as the Nexus 4's and 5's) and the shim it preloads.
SRC_URI += " \
    file://camera-provider-wait.sh \
    file://android.hardware.camera.provider@2.4-service.rc \
"
HALIUM_LEGACY_EXTRA_FILES += " \
    a9/halium-legacy-a9-services/bin/hw/android.hardware.camera.provider@2.4-service:/bin/hw/android.hardware.camera.provider@2.4-service:755 \
    camera-provider-wait.sh:/bin/camera-provider-wait.sh:755 \
    android.hardware.camera.provider@2.4-service.rc:/etc/init/android.hardware.camera.provider@2.4-service.rc:644 \
"
HALIUM_LEGACY_BINDERIZED_HALS = "android.hardware.camera.provider"
