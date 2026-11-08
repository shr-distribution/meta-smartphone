require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

# The GSI is the shared include's, used as it is published: it has the /efs and /persist directories
# this vendor's fstab mounts (20261005-2 added /efs for it).

COMPATIBLE_MACHINE = "^sm-t520$"

# The vendor image published with the GSI (sm-t520-vendor.img in its release), made by the settings below
# against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one; that needs the local
# device tarball below.
HALIUM_LEGACY_VENDOR_SHA256 = "2f2f086c3a9ec3fd30b54b80a44c34fc5c4860dd5742f20b36d192a6d81b6ecc"

# The 32-bit 16.0 GSI (HALIUM_LUNEOS_GSI16_* in android-system-image-legacy-gsi.inc)
# on a vendor that comes from the exynos5420 community's LineageOS 18.1 trees.
#
# That build is Android 11 (VNDK 30, vendor manifest target-level "legacy") and
# has no vendor partition: it puts the vendor under /system/vendor, and it leaves
# the Samsung/Lineage HAL libraries in /system/lib. So it goes through the same
# conversion as hammerhead, mako and tenderloin (halium-legacy-vendor), with VNDK
# 30 instead of 28 and no init scripts to move: the A11 vendor already ships its
# init files in /vendor/etc/init. The GSI carries com.android.vndk.v30 for 32-bit
# ARM (PRODUCT_EXTRA_VNDK_VERSIONS 27..34 in device/halium/halium_arm).
HALIUM_LEGACY_VNDK = "30"

# The "device" tarball is a system.img laid out like a monolithic Halium build:
# a root holding system/, with the vendor under system/vendor. It is assembled from
# the Halium 11 build of lineage_n2awifi by assemble-old-system.sh (kept with the
# port's scratch files, not in this layer), because that tree's systemimage target
# does not build (the ART apex has no native libraries for this 32-bit product, which
# does not matter for a vendor). Published in the halium-luneos-20261108 release of
# webOS-ports/halium-images.
#
# The 20261005-1 tarball differs from the 20261003-1 one in four libraries: libandroidicu.so, libicuuc.so,
# libicui18n.so and libnativehelper.so are the real Android 11 files from the build's ART apex, where they
# were links to it before, so the converter now puts them in /vendor/lib. The camera HAL
# (camera.universal5420.so -> libharfbuzz_ng.so -> libandroidicu.so) and gpsd (libicuuc.so) could not
# load without them. 20261005-2 adds libstatssocket.so and libstatspull.so (statsd apex), which the
# libandroid_runtime.so that comes along under the camera HAL needs.
SM_T520_DEVICE_TARBALL ?= "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-20261108/halium-luneos-11.0-20261108-1-sm-t520.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${SM_T520_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "441662962a1e389c3c2b05a30ca6efe49d44aff6e3c659846eda1a1f79567f36"

# No device init scripts to move (see above); everything the vendor needs is in
# /vendor/etc/init already.
HALIUM_LEGACY_INIT_FILES = ""

# The bcmdhd driver reads its firmware and nvram itself, from the paths compiled into the kernel
# (CONFIG_BCMDHD_FW_PATH, CONFIG_BCMDHD_NVRAM_PATH). linux-samsung-sm-t520's luneos.cfg sets them to
# /etc/wifi, which android-system-image-legacy-gsi.inc links to /vendor/etc/wifi on the host, so the
# files go in the vendor image. nvram_net.txt is not named by any binary and has to be listed; without it
# the driver fails with "dongle nvram file download failed" and wlan0 never comes up (4 Oct 2026).
HALIUM_LEGACY_COPY_FILES = " \
    /system/etc/wifi/bcmdhd_sta.bin:/etc/wifi/bcmdhd_sta.bin \
    /system/etc/wifi/bcmdhd_apsta.bin:/etc/wifi/bcmdhd_apsta.bin \
    /system/etc/wifi/nvram_net.txt:/etc/wifi/nvram_net.txt \
    ${SM_T520_OMX_CODECS} \
"
HALIUM_LEGACY_GSI_LINKS = ""

# The Exynos OMX codecs, for the hardware video encoder that recording needs. Nothing links them: the
# OMX service dlopen()s libstagefrighthw.so, which dlopen()s libExynosOMX_Core.so, which loads the
# codecs from a directory compiled into it (see HALIUM_LEGACY_BYTE_PATCHES). So the converter cannot
# find them by itself, and without them droidmedia finds no video encoder and recording fails at start.
SM_T520_OMX_CODECS = " \
    /system/lib/omx/libOMX.Exynos.AVC.Decoder.so:/lib/omx/libOMX.Exynos.AVC.Decoder.so \
    /system/lib/omx/libOMX.Exynos.AVC.Encoder.so:/lib/omx/libOMX.Exynos.AVC.Encoder.so \
    /system/lib/omx/libOMX.Exynos.MPEG4.Decoder.so:/lib/omx/libOMX.Exynos.MPEG4.Decoder.so \
    /system/lib/omx/libOMX.Exynos.MPEG4.Encoder.so:/lib/omx/libOMX.Exynos.MPEG4.Encoder.so \
    /system/lib/omx/libOMX.Exynos.VP8.Decoder.so:/lib/omx/libOMX.Exynos.VP8.Decoder.so \
    /system/lib/omx/libOMX.Exynos.VP8.Encoder.so:/lib/omx/libOMX.Exynos.VP8.Encoder.so \
    /system/lib/omx/libOMX.Exynos.WMV.Decoder.so:/lib/omx/libOMX.Exynos.WMV.Decoder.so \
"

# The camera HAL (camera.universal5420.so) links the Android 11 framework copies the converter puts in
# /vendor/lib (libgui, libcamera_client, libhwui, ...), and libhwui.so needs libft2.so. The GSI's LLNDK
# list names libft2.so, so the converter counts it as provided, but the vendor linker of the container
# does not hand it to this vendor: "library libft2.so not found: needed by /vendor/lib/libhwui.so", and
# the camera provider restarts for ever (5 Oct 2026). A private copy from the old image fixes it.
HALIUM_LEGACY_EXTRA_LIBS = "libft2.so libstagefrighthw.so libExynosOMX_Core.so libExynosOMX_Resourcemanager.so"

# The camera HAL (camera.universal5420.so -> libexynoscamera.so -> libcamera_client.so, libgui.so, ...)
# brings Android 11 framework libraries with it, and those want symbols of the full libbinder and libmedia
# that the VNDK 30 snapshot of the GSI does not have (PermissionCache, AppOpsManager, Parcel helpers, ...).
# halium-legacy-shim provides them; the converter adds it as a dependency of these libraries.
HALIUM_LEGACY_SHIM_TARGETS = "libgui.so libmedia.so libmediautils.so libsensor.so libandroid_runtime.so libmedia_codeclist.so"

# What LineageOS' linker does for TARGET_LD_SHIM_LIBS in device/samsung/universal5420-common
# (/vendor/lib/libexynoscamera.so|/vendor/lib/libshim_camera.so): the camera HAL needs the
# CameraParameters constants and getInt64() of that shim. The Android 16 linker has no such feature, so
# the shim becomes a dependency of libexynoscamera.so. (The same file also names libGLES_mali.so|libgutils.so
# and gpsd|libshim_dmitry_gps.so; the GPU works without the first and the Wi-Fi tablet has no GPS.)
HALIUM_LEGACY_LD_SHIMS = "libexynoscamera.so|libshim_camera.so"

# libExynosOMX_Core.so loads the codecs from /system/lib/omx/, which in the container is the GSI's:
# point it at /vendor/lib/omx/ (HALIUM_LEGACY_COPY_FILES above), same length.
#
# libexynoscamera.so (after HALIUM_LEGACY_LD_SHIMS added libshim_camera.so to it): the close-race fixes
# of device/samsung/universal5420-common hardware/camera/vendor-patches/libexynoscamera, whose build.sh
# makes the patched file (md5 f85bcb7703478b105e37996e882c7620). As byte patches: the first PT_LOAD's
# file and memory size grown over the code caves; the b.w over stopPreview()'s AF-thread stop and over
# takePicture()'s m_captureInProgress check; and the two code caves in the zero padding after that
# segment. takePicture() waits for the previous picture thread to have ended; stopPreview() lets a
# picture that is still being taken finish, then stops the autofocus thread without losing the
# m_autoFocusLock handoff. Without them a close with a picture pending can hang the camera for good.
HALIUM_LEGACY_BYTE_PATCHES = " \
    /lib/libExynosOMX_Core.so:17f2:73797374656d:76656e646f72 \
    /lib/libexynoscamera.so:84:3815060038:dc150600dc \
    /lib/libexynoscamera.so:3dcf2:d4f84c01:23f055bc \
    /lib/libexynoscamera.so:3e88c:94f81115:22f058be \
    /lib/libexynoscamera.so:61540:00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000:94f8111511b12269cab905e0d4f85001a8b1b7f7e0e990b103a8b9f7e2fed4f8500108b1b5f72aeb0398b9f7e1fe94f8450230b194f8111509b1ddf78cb9ddf794b9ddf77eb9 \
    /lib/libexynoscamera.so:615a0:000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000:d4f8500130b1b7f7b6e918b1d4f85001b5f704ebd4f84c0170b1012684f8306204f50b70b9f7b4fe04f50b70b5f732ebd4f84c01b5f7f2eadcf798bb \
"

# Nothing of the old image goes into the GSI. The vendor's fstab (fstab.universal5420) mounts EFS on /efs
# and PERSDATA on /persist, which the GSI has itself; EFS holds the Bluetooth address bcm-hciattach.sh reads
# (/android/efs/bluetooth/bt_addr) and the camera calibration. What the vendor names under /system/etc is
# for what LuneOS does not run here: bt_vendor.conf is for libbt-vendor.so, the Android Bluetooth HAL that
# bcm-hciattach replaced; gps.conf for gpsd on a tablet without GPS; audio_effects.conf for libeffects, the
# Android effects framework; boot-image.prof and dirty-image-objects for ART.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# This vendor already declares the composer and allocator hwbinder, so there is
# nothing to rewrite in its manifest.
HALIUM_LEGACY_BINDERIZED_HALS = ""
