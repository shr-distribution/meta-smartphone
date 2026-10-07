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

# 20261007-1 adds the /persist and /efs directories and the /firmware link that halium_arm got
# on 5 Oct 2026, for pre-Treble arm64 vendors (bullhead).
HALIUM_LUNEOS_GSI16_PV ?= "20261007-1"
HALIUM_LUNEOS_GSI16_SHA256 ?= "89d73b83a161e9ef5270aab803b4120519f02144aa8df631fac0a86eaa1a84ea"

# 32-bit ports (mindphone) set this to halium_arm and bring their own PV/SHA -
# the arm GSI is a separate build and is not published on the same cadence.
HALIUM_LUNEOS_GSI16_ARCH ?= "halium_arm64"

# The release TAG drops the -N suffix that PV carries: PV 20260910-1 lives under
# the tag halium-luneos-20260910. Derived rather than written out again, because
# a second place to edit is how this file came to be needed.
# Exception: the 20261007 builds (halium_arm and halium_arm64) were published in the
# halium-luneos-20261005 release, next to the vendor images made for them.
HALIUM_LUNEOS_GSI16_RELEASE ?= "halium-luneos-${@{'20261007': '20261005'}.get(d.getVar('HALIUM_LUNEOS_GSI16_PV').split('-')[0], d.getVar('HALIUM_LUNEOS_GSI16_PV').split('-')[0])}"

HALIUM_LUNEOS_GSI16_TARBALL ?= "halium-luneos-16.0-${HALIUM_LUNEOS_GSI16_PV}-${HALIUM_LUNEOS_GSI16_ARCH}.tar.bz2"
HALIUM_LUNEOS_GSI16_URL ?= "https://github.com/webOS-ports/halium-images/releases/download/${HALIUM_LUNEOS_GSI16_RELEASE}/${HALIUM_LUNEOS_GSI16_TARBALL}"
