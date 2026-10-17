require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

# The same 32-bit GSI the Galaxy A3 (2015) pins (see android-system-image-a3-2015-halium.bb for what 20261008-3
# has over the shared include's default): the sphal namespace links libbinder.so and libui.so from the VNDK
# APEX, which a vendor's gralloc needs.
# 20261017-1 is 20261008-3 with droidmedia's buffer and codec fixes (the only file that differs) and 512 free
# inodes instead of 64. The vendor image below, made against 20261008-3, stays: a vendor image derived against
# either GSI has the same contents (checked on the TouchPad).
HALIUM_LUNEOS_GSI16_PV = "20261017-1"
HALIUM_LUNEOS_GSI16_SHA256 = "0984f47ea33ca7e058a4db7e9e5145760d5b433b19e14e8ba22a38d12648a871"
HALIUM_LUNEOS_GSI16_RELEASE = "halium-luneos-20261005"

# The vendor image published with the GSI: goyavewifi-vendor-20261017.img in the halium-luneos-20261005
# release, the UNPACKDIR/vendor.img of a build of this recipe from the 20261017-1 device tarball below against
# GSI 20261008-3. Over the tester kit v15 it has the sensors HAL and /vendor/etc/audio_policy.conf. Dated, so it
# does not replace the release's older goyavewifi-vendor.img (20261008-2 tarball, without the fstab and
# init.sc8830.rc fixes). The settings below stay as the record of how it was made; see
# android-system-image-legacy-gsi.inc for how to make a new one.
HALIUM_LEGACY_VENDOR_URL = "https://github.com/webOS-ports/halium-images/releases/download/${HALIUM_LUNEOS_GSI16_RELEASE}/goyavewifi-vendor-20261017.img"
HALIUM_LEGACY_VENDOR_SHA256 = "7ded73a56adda77eca4f0bf67158ba2c2f0c7c4910ddd27b407d257fc204aa36"

COMPATIBLE_MACHINE = "^goyavewifi$"

# The 32-bit 16.0 GSI, on a vendor built from the LineageOS 16.0 (Android 9, VNDK 28) trees of the tablet
# (github.com/sc8830-Bringup, branches lineage-16.0: android_device_samsung_goyavewifi, ..._scx35-common,
# android_vendor_samsung_goyavewifi, android_hardware_sprd and android_kernel_samsung_sc8330; scx30g-common only has
# lineage-15.1 there, and device/samsung/sprd-common is Vuhased's lineage-15.1-wip fork).
#
# That build has no vendor partition either: it puts the vendor under /system/vendor and keeps the board's init
# scripts and fstab in the boot ramdisk. So it goes through the same conversion as the A3 (2015), the SM-T520,
# hammerhead, mako and tenderloin (halium-legacy-vendor), with the board's init scripts to move. Android 9 is
# VNDK 28, which the GSI populates (224 libraries). The earlier LineageOS 15.1 (Android 8.1) vendor does not work
# as well: the GSI declares com.android.vndk.v27 but its APEX is empty, so the 8.1 vendor had to be linked
# against VNDK 28 all the same, and its build has no composer HAL.
HALIUM_LEGACY_VNDK = "28"

# The "device" tarball is the system.img of that build laid out like a monolithic Halium build: a root holding
# system/ (the vendor under system/vendor) plus the board's init scripts, ueventd rules and fstab from the
# build's root/. The Halium 9.0 tree builds it as lunch lineage_goyavewifi-userdebug, "m -k systemimage" and
# then "m snod" (the one failing target, libsuspend, is a system library the vendor does not use); see
# goyavewifi-STATUS.md. tools/assemble-device.py of the port's working directory lays it out, with the build's
# own ownership and modes. Not published anywhere yet, so it is a local file.
# TODO: publish to webOS-ports/halium-images and use an https URL.
GOYAVEWIFI_DEVICE_TARBALL ?= "file:///home/herrie/webos/LuneOS/samsung/sm-t113/out-device-16/halium-luneos-9.0-20261017-1-goyavewifi.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${GOYAVEWIFI_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "2fe3eeb0b28d33affba7f4ff352c527f6c9d484fa9c79be50910576fbd7e1066"

# The board scripts of the boot ramdisk, which move to /vendor/etc/init/hw. init.sc8830.usb.rc is left out on
# purpose: the container's init must not write the USB gadget the host's adbd owns. The ueventd rules become
# /vendor/etc/ueventd.rc.
HALIUM_LEGACY_INIT_FILES = "init.sc8830.rc init.board.rc init.wifi.rc ueventd.sc8830.rc"
# The audio policy of the Lineage 16 build is the legacy audio_policy.conf in its /system/etc, which the plain GSI
# replaces. pulseaudio-distro-conf's droid-audio-config-gen looks for it in /android/vendor/etc and, finding nothing,
# loads module-droid-card with no config: "droid-config.c: Failed to parse any configuration", so no droid card, no
# pcm_output sink and no sound (boot journal of 14 Oct 2026, the audio HAL service itself was running). The HP TouchPad
# (tenderloin) needs the same copy.
HALIUM_LEGACY_COPY_FILES += "/system/etc/audio_policy.conf:/etc/audio_policy.conf"

# The plain GSI: nothing of the old image goes into it.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# The gralloc module (gralloc.sc8830.so -> libdither.so -> libandroid_runtime.so -> libpdfium.so -> libft2.so) is
# what both graphics HAL services open. The ninth-round logcat (12 Oct 2026):
#
#   vndksupport: Could not load /vendor/lib/hw/gralloc.sc8830.so from default namespace: dlopen failed:
#   library "libft2.so" not found: needed by /vendor/lib/libpdfium.so
#   AllocatorHal: failed to get gralloc module / ComposerHal: failed to open hwcomposer device
#
# so allocator and composer exit with status 1 at every start. The GSI's LLNDK list names libft2.so, so the
# converter used to count it as provided, but the vendor's linker namespace does not hand it to this vendor. Same
# finding as the SM-T520 and the A3 (2015) (and the TouchPad): a private copy from the old image is needed.
# (halium-legacy-vendor revision 32 pulls libft2.so by itself when something in the vendor needs it; nothing to set.)

# The framework copies the vendor brings along (libgui, libmedia, libmediautils, libsensor, libandroid_runtime)
# want the permission, AppOps and process-info helpers of the full libbinder that the VNDK 28 snapshot leaves out
# (PermissionCache, AppOpsManager, IPermissionController, IProcessInfoService, IBatteryStats: undefined in every
# library of vendor, VNDK 28 and bionic). halium-legacy-shim provides them, as for the two siblings.
HALIUM_LEGACY_SHIM_TARGETS = "libgui.so libmedia.so libmediautils.so libsensor.so libandroid_runtime.so libnativeloader.so libgraphicsenv.so libC.so libmemunreachable.so libpdx_default_transport.so"
