# Single source of truth for the Halium/LuneOS 16.0 GSI.
#
# WHY THIS EXISTS
#
# Every android-system-image-<device> recipe used to carry its own copy of the
# release tag, tarball name and sha256. They drifted, silently, and the drift is
# not visible from any one recipe: on 24 Sep 2026 tissot and mido were on
# 20260910-1 while sargo, athena and radon were still on 20260826-1.
#
# That cost a day on radon. The 20260826 GSI ships com.android.vndk.v32 and v34
# only; 20260910 is the first build with
#
#     PRODUCT_EXTRA_VNDK_VERSIONS := 27 28 29 30 31 32 33 34
#
# from device/halium/halium_arm64/device.mk, whose stated intent is that "one
# arm64 GSI serves any vendor from Android 11 to 14 and adding a device never
# means rebuilding the GSI". radon's vendor wants v31/v32; with only v32 and v34
# present libhybris' vndk_apex_lib_dir() found nothing, HYBRIS_PREFER_VNDK
# silently became a no-op, and the device had no audio - with the flag set and
# nothing anywhere saying why.
#
# So: bump it here, once. A device that genuinely needs to be held back can still
# override any of these - they are all weak assignments - but it then says so in
# its own recipe, where the next reader will see it.
#
# HOW TO BUMP
#
#   1. publish the release at github.com/webOS-ports/halium-images
#   2. set HALIUM_LUNEOS_GSI16_PV below
#   3. set HALIUM_LUNEOS_GSI16_SHA256 to the sha256 of the arm64 tarball
#   4. rebuild every machine that uses it - they all move together by design
#
# Verify a candidate actually carries the VNDK snapshots before trusting it:
#
#   tar xjf halium-luneos-16.0-<PV>-halium_arm64.tar.bz2 system.img
#   debugfs -R "ls /system_ext/apex" system.img | tr ' ' '\n' | grep vndk
#
# (they live in /system_ext/apex, not /system/apex).

# 20261108-1 (halium_arm64 and halium_arm) differs from 20261107-2 in linkerconfig,
# which reads a vendor binary's target SDK from a .version file next to it
# (enable.target.sdk.version in the vendor section), in CameraClient, which hands HAL1
# recording frames back to the HAL off its callback thread, and in init.halium.rc,
# which preloads libselinux_stubs.so for vndservicemanager by soname again (20261107-2
# was built with the /system/lib64 path). It still carries com.android.vndk.v27 to v34.
# 20261107-2 (halium_arm64; halium_arm is 20261107-1) differs from 20261104-1
# only in droidmedia: /system/lib{,64}/libdroidmedia.so, /system/bin/minimediaservice
# and build.prop's dates. It still carries com.android.vndk.v27 to v34.
# 20261104-1 carries droidmedia's decoder fixes (herrie/video-playback): a codec
# that fails wakes the reader waiting for its output instead of leaving it
# waiting for ever, the reader thread is stopped before its source is freed, an
# input frame larger than the codec's buffer is refused instead of overflowing
# it, and the buffer queue's slots are emptied under its lock. Same content as
# 20261103-1 otherwise (libdroidmedia.so, 32- and 64-bit, and build dates).
# 20261103-1 adds the Widevine bridge's Android half (wvbridged, wvlaunch and
# libwvshim.so in /system, webOS-ports/luneos-widevine-bridge) and the
# linkerconfig link that lets the vendor's Widevine HAL load libwvshim.so. None
# of it runs unless the bridge is turned on in LuneOS. Same content as
# 20261017-1 otherwise.
# 20261017-1 carries droidmedia's buffer and codec fixes (herrie/video-playback:
# the consumer may hold 8 buffers, not 4, which a decoder with a long output delay
# needs to keep up; a codec that was never created is not released).
# 20261014-1 keeps >=512 free inodes in system.img (build_image.py). The stock
# AOSP image is sized with almost no spare inodes, so the per-device debugfs
# patching (tissot's /system/etc/camera chromatix XMLs, the /firmware symlinks,
# the vendor stubs) ran out of inodes and silently dropped files - tissot's front
# camera never got its chromatix and enumerated zero cameras from an image that
# built and fsck'd clean. Same content as 20261007-1 otherwise.
# 20261007-1 added the /persist and /efs directories and the /firmware link that
# halium_arm got on 5 Oct 2026, for pre-Treble arm64 vendors (bullhead).
HALIUM_LUNEOS_GSI16_PV ?= "20261108-1"
HALIUM_LUNEOS_GSI16_SHA256 ?= "5d913d3e35f4a405bcfc36b91ca356b8837216269c06c43e3ed7be101f8cda00"

# 32-bit ports (mindphone) set this to halium_arm and bring their own PV/SHA -
# the arm GSI is a separate build and is not published on the same cadence.
HALIUM_LUNEOS_GSI16_ARCH ?= "halium_arm64"

# The release TAG drops the -N suffix that PV carries: PV 20260910-1 lives under
# the tag halium-luneos-20260910. Derived rather than written out again, because
# a second place to edit is how this file came to be needed.
# Exception: the 20261007, 20261014 and 20261017 builds (halium_arm and halium_arm64) were
# published in the halium-luneos-20261005 release, next to the vendor images made
# for them.
HALIUM_LUNEOS_GSI16_RELEASE ?= "halium-luneos-${@{'20261007': '20261005', '20261014': '20261005', '20261017': '20261005'}.get(d.getVar('HALIUM_LUNEOS_GSI16_PV').split('-')[0], d.getVar('HALIUM_LUNEOS_GSI16_PV').split('-')[0])}"

HALIUM_LUNEOS_GSI16_TARBALL ?= "halium-luneos-16.0-${HALIUM_LUNEOS_GSI16_PV}-${HALIUM_LUNEOS_GSI16_ARCH}.tar.bz2"
HALIUM_LUNEOS_GSI16_URL ?= "https://github.com/webOS-ports/halium-images/releases/download/${HALIUM_LUNEOS_GSI16_RELEASE}/${HALIUM_LUNEOS_GSI16_TARBALL}"
