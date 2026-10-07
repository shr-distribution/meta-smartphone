# The Android 9 services a pre-Treble Halium 9 vendor needs under the 16.0 GSI.
#
# A monolithic Halium 9 build never ran its HIDL HALs as services of their own (hybris loaded the legacy
# HAL modules in process), and the Android 16 userspace expects services. These are the A9 tree's own
# service wrappers, built 32-bit (see BUILD.md in the tarball); none is specific to a device, they call
# whatever legacy HAL module the vendor has. They go with halium-legacy-vendor (see
# android-system-image-legacy-gsi.inc, which the recipe requires too).
#
# A recipe that requires this file gets the graphics services, the crash_dump stand-in and the
# permission-cache shim. What only some devices have is a set of variables to add to
# HALIUM_LEGACY_EXTRA_FILES (and the matching HALIUM_LEGACY_BINDERIZED_HALS entry):
#
#   HALIUM_LEGACY_A9_CAMERA   camera provider, for a device with a camera HAL (camera.<board>.so)
#   HALIUM_LEGACY_A9_RIL      rild and the libril it links, for a device with a Qualcomm modem
#   HALIUM_LEGACY_A9_OMX      the OMX service, for a device with vendor OMX codecs (libstagefrighthw.so);
#                             hybris-resource-manager.rc is what lets the recorder create an encoder
#
# The services that need it load /vendor/lib/libpermissioncache_shim.so: the vendor's copy of libgui and
# libmedia want android::PermissionCache from the old libbinder, which the VNDK snapshot of the GSI lacks.
# It also lowers the target SDK version of the camera provider, so the Android 16 linker loads the
# camera HAL's libraries that have text relocations.

FILESEXTRAPATHS:prepend := "${THISDIR}/android-system-image-legacy-a9-services:"

SRC_URI += " \
    file://halium-legacy-a9-services.tar.xz;subdir=a9 \
    file://android.hardware.graphics.composer@2.1-service.rc \
    file://android.hardware.graphics.allocator@2.0-service.rc \
    file://crash-dump-noop.rc \
    file://android.hardware.camera.provider@2.4-service.rc \
    file://camera-provider-wait.sh \
    file://rild.rc \
    file://rild-wait.sh \
    file://android.hardware.media.omx@1.0-service.rc \
    file://hybris-resource-manager.rc \
    file://permissioncache_shim.c \
    file://shim.map \
"

HALIUM_LEGACY_A9_BIN = "a9/halium-legacy-a9-services"

HALIUM_LEGACY_EXTRA_FILES += " \
    ${HALIUM_LEGACY_A9_BIN}/bin/hw/android.hardware.graphics.composer@2.1-service:/bin/hw/android.hardware.graphics.composer@2.1-service:755 \
    ${HALIUM_LEGACY_A9_BIN}/bin/hw/android.hardware.graphics.allocator@2.0-service:/bin/hw/android.hardware.graphics.allocator@2.0-service:755 \
    ${HALIUM_LEGACY_A9_BIN}/bin/crash_dump_noop:/bin/crash_dump_noop:755 \
    ${HALIUM_LEGACY_A9_BIN}/lib/libpermissioncache_shim.so:/lib/libpermissioncache_shim.so:644 \
    android.hardware.graphics.composer@2.1-service.rc:/etc/init/android.hardware.graphics.composer@2.1-service.rc:644 \
    android.hardware.graphics.allocator@2.0-service.rc:/etc/init/android.hardware.graphics.allocator@2.0-service.rc:644 \
    crash-dump-noop.rc:/etc/init/crash-dump-noop.rc:644 \
"

HALIUM_LEGACY_A9_CAMERA = " \
    ${HALIUM_LEGACY_A9_BIN}/bin/hw/android.hardware.camera.provider@2.4-service:/bin/hw/android.hardware.camera.provider@2.4-service:755 \
    camera-provider-wait.sh:/bin/camera-provider-wait.sh:755 \
    android.hardware.camera.provider@2.4-service.rc:/etc/init/android.hardware.camera.provider@2.4-service.rc:644 \
"
HALIUM_LEGACY_A9_RIL = " \
    ${HALIUM_LEGACY_A9_BIN}/bin/hw/rild:/bin/hw/rild:755 \
    ${HALIUM_LEGACY_A9_BIN}/lib/libril.so:/lib/libril.so:644 \
    ${HALIUM_LEGACY_A9_BIN}/lib/librilutils.so:/lib/librilutils.so:644 \
    rild-wait.sh:/bin/rild-wait.sh:755 \
    rild.rc:/etc/init/rild.rc:644 \
"
HALIUM_LEGACY_A9_OMX = " \
    ${HALIUM_LEGACY_A9_BIN}/bin/hw/android.hardware.media.omx@1.0-service:/bin/hw/android.hardware.media.omx@1.0-service:755 \
    ${HALIUM_LEGACY_A9_BIN}/lib/libavservices_minijail_vendor.so:/lib/libavservices_minijail_vendor.so:644 \
    android.hardware.media.omx@1.0-service.rc:/etc/init/android.hardware.media.omx@1.0-service.rc:644 \
    hybris-resource-manager.rc:/etc/init/hybris-resource-manager.rc:644 \
"

# vss_init is the voice-call helper. It links libril.so, which neither the old Halium 9 image nor the
# GSI provides, so on a device with a modem it only ever crash-loops.
HALIUM_LEGACY_SKIP_SERVICES = "charger bugreport vss_init"

# libdroidmedia, which gst-droid loads on the host, links the GSI's libstagefright, and that wants
# libcom.android.media.swcodec.apexcodecs.so, a library of the media.swcodec APEX. hybris looks for it in the
# system, vendor and odm library directories and not in an APEX, so put it where it looks (/odm is the host
# directory the container's /odm is bound to). The APEX is mounted by mount-android.sh, from
# /etc/android-host-apexes, which a device that wants its media APEXes on the host lists.
do_install:append() {
    install -d ${D}/odm/lib
    ln -sf /apex/com.android.media.swcodec/lib/libcom.android.media.swcodec.apexcodecs.so \
        ${D}/odm/lib/libcom.android.media.swcodec.apexcodecs.so
}
FILES:${PN} += "/odm"
# The link points into an APEX that only exists on the running device, and is not a development symlink.
INSANE_SKIP:${PN} += "dev-so"
