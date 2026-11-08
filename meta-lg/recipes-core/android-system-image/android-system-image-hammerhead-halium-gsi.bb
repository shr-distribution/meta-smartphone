require recipes-core/android-system-image/android-system-image-legacy-gsi.inc
require recipes-core/android-system-image/android-system-image-legacy-a9-services.inc

COMPATIBLE_MACHINE = "hammerhead-halium"

# The vendor image published with the GSI (hammerhead-halium-vendor.img in its release), made by the settings below
# against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one.
HALIUM_LEGACY_VENDOR_SHA256 = "64514a42ec48d08ca99cedbb890062a5dc3954d5cc5f52b3c3464a31b57a61b3"

# Ahead of the other legacy ports: 20261108-1 is the first halium_arm GSI whose linkerconfig turns on
# enable.target.sdk.version for the vendor section, which the /vendor/bin/.version below needs. Its
# CameraClient also hands HAL1 recording frames back off the HAL's callback thread. The others move
# once their vendor images are in that release too.
HALIUM_LUNEOS_GSI16_PV = "20261108-1"
HALIUM_LUNEOS_GSI16_SHA256 = "563c43e5b22e3ff76e0f17f8c12bca1d69ccb76c2e72f6f271473d30c30e3065"

HALIUM_LEGACY_DEVICE_URL = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-20230127-1-hammerhead-halium.tar.bz2/halium-luneos-9.0-20230127-1-hammerhead-halium.tar.bz2"
HALIUM_LEGACY_DEVICE_SHA256 = "a3b058dd2bafd6d9af661b03f54cb35e115dd5601624534e34a271bfde1b1e46"

# init.hammerhead.usb.rc is left out on purpose, and so is its import: it writes
# /sys/class/android_usb/android0 (73 writes), the gadget the host's adbd uses,
# and a container's init has no business managing it. adb was disappearing at an
# unpredictable point in boot while it was loaded; that it is the cause has not
# been confirmed. There is also no init.hammerhead.power.rc in this image,
# unlike mako and tenderloin.
HALIUM_LEGACY_INIT_FILES = "init.hammerhead.rc ueventd.hammerhead.rc"

# bcmdhd's compiled-in NVRAM path is /etc/wifi/bcmdhd.cal (CONFIG_BCMDHD_NVRAM_PATH); without
# it "dongle nvram file download failed" and wlan0 never comes up.
#
# The audio HAL (loaded into PulseAudio by libhybris) looks for mixer_paths.xml in /vendor/etc,
# /system/etc and /odm/etc, and libacdbloader reads the calibration from /etc/acdbdata, which
# the image links to /vendor/etc/acdbdata. Without them "Failed to open audio hw device".
HALIUM_LEGACY_ACDB = "MTP_Global_cal MTP_Bluetooth_cal MTP_Hdmi_cal MTP_General_cal MTP_Headset_cal MTP_Speaker_cal MTP_Handset_cal"
HALIUM_LEGACY_COPY_FILES = "/system/etc/wifi/bcmdhd.cal:/etc/wifi/bcmdhd.cal \
    /system/etc/mixer_paths.xml:/etc/mixer_paths.xml \
    /system/etc/media_codecs.xml:/etc/media_codecs.xml \
    /system/etc/media_codecs_google_audio.xml:/etc/media_codecs_google_audio.xml \
    /system/etc/media_codecs_google_telephony.xml:/etc/media_codecs_google_telephony.xml \
    /system/etc/media_codecs_google_video.xml:/etc/media_codecs_google_video.xml \
    /system/etc/media_codecs_performance.xml:/etc/media_codecs_performance.xml \
    ${@' '.join('/system/etc/acdbdata/MTP/%s.acdb:/etc/acdbdata/MTP/%s.acdb' % (n, n) for n in d.getVar('HALIUM_LEGACY_ACDB').split())} \
"

# The NFC HAL reads its base config and the chip config (libnfc-nci.conf and libnfc-nci-<chip id>.conf,
# the chip id 20791b05 being what the BCM20793 reports) from the first of /odm/etc/, /vendor/etc/ and /etc/ that
# has them. Without them it runs on built-in defaults, the chip never answers the first NCI command
# ("nfc_hal_nci_cmd_timeout_cback", pre_init status 3) and no tag is ever detected. The vendor image is the
# right place; the old image calls the chip config libnfc-brcm-20791b05.conf.
HALIUM_LEGACY_COPY_FILES += "/system/etc/libnfc-nci.conf:/etc/libnfc-nci.conf \
    /system/etc/libnfc-brcm-20791b05.conf:/etc/libnfc-nci-20791b05.conf"

# The camera daemon builds libmmcamera_<sensor>.so and libchromatix_<sensor>_<mode>.so names from the sensor
# names the kernel reports (imx179 rear, mt9m114b front), so no ELF names them and the module cannot find them.
# Without them mm-qcamera-daemon adds no sensor data to the capability struct (both cameras come back identical
# and almost empty), camera.hammerhead.so overruns a stack array in initStaticMetadata and the camera provider
# dies; with them the provider registers with two camera devices.
HALIUM_LEGACY_EXTRA_LIBS = "libmmcamera_imx179.so libmmcamera_mt9m114b.so \
    libchromatix_imx179_common.so libchromatix_imx179_default_video.so \
    libchromatix_imx179_preview.so libchromatix_imx179_snapshot.so \
    libchromatix_mt9m114b_common.so libchromatix_mt9m114b_default_video.so \
    libchromatix_mt9m114b_preview.so libchromatix_mt9m114b_snapshot.so"

# The OMX codecs (Qualcomm's OpenMAX core and the hardware components) are loaded by name from
# media_codecs.xml and by libstagefrighthw, so no ELF names them either. The GSI's libstagefright wants
# the codecs behind a HIDL service (android.hardware.media.omx@1.0), which a monolithic Halium 9
# build never ran: hybris' minimediaservice loaded them in process. See HALIUM_LEGACY_EXTRA_FILES.
HALIUM_LEGACY_EXTRA_LIBS += "libril-qc-qmi-1.so libOmxCore.so libOmxVdec.so libOmxVenc.so \
    libmm-omxcore.so libstagefrighthw.so libdivxdrmdecrypt.so libqdMetaData.so libc2dcolorconvert.so"

# The services a monolithic Halium 9 build never ran on their own, and the init scripts that start them, come
# from android-system-image-legacy-a9-services.inc, which every legacy device shares. The Nexus 5 has all of
# the optional sets: a camera HAL, a Qualcomm modem and vendor OMX codecs. The camera provider is a service of
# its own too, and has to be reached over hwbinder.
HALIUM_LEGACY_EXTRA_FILES += " \
    ${HALIUM_LEGACY_A9_CAMERA} \
    ${HALIUM_LEGACY_A9_RIL} \
    ${HALIUM_LEGACY_A9_OMX} \
"
# The camera daemon (/vendor/bin/mm-qcamera-daemon, Android 6.0.1 M4B30Z, SDK 23) locks a mutex its own
# teardown has already destroyed. Bionic answers that with EBUSY for a binary that targets SDK 27 or
# lower and aborts for SDK 28 and up, and a native vendor binary gets the GSI's SDK unless the linker
# finds a .version next to it. The GSI's linkerconfig turns that lookup on for the vendor section; this
# file makes it 23 for everything in /vendor/bin (not /vendor/bin/hw, which is a directory of its own).
# Without it the daemon aborts on that lock.
SRC_URI += "file://vendor-bin-version"
HALIUM_LEGACY_EXTRA_FILES += "vendor-bin-version:/bin/.version:644"

# camera.hammerhead.so frees the queued jpeg_settings_t twice when the camera closes while a picture is still
# being encoded: ~QCamera3PostProcessor destroys m_jpegSettingsQ (this+0x114), whose entries the JPEG path
# has already freed, and the provider dies with a double free. Two Thumb NOPs replace that one destructor
# call (the blx at 0x1892e): the queue is not torn down, so what it still holds leaks once per close. Unpatched, the HAL crashed by
# cycle 27 of a close-with-a-picture-pending camcycle run; patched it passed 100.
HALIUM_LEGACY_BYTE_PATCHES = "/lib/hw/camera.hammerhead.so:1892e:f2f77cef:00bf00bf"

HALIUM_LEGACY_BINDERIZED_HALS = "android.hardware.graphics.composer android.hardware.graphics.allocator \
    android.hardware.camera.provider"

# The GSI is the plain halium_arm one (it has /persist and /firmware itself): nothing of the old
# image goes into it. The only /system/etc file the vendor names is DxHDCP.cfg, the HDCP DRM blob.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# bluez's hciattach looks for the Broadcom patch RAM in /usr/lib/firmware, as <chip name>.hcd.
# Without it "Patch not found, continue anyway": the controller runs its ROM (HCI revision 0x0)
# and receives no advertisements at all. The patch is the vendor's, in /vendor/firmware.
do_install:append() {
    install -d ${D}${nonarch_libdir}/firmware
    ln -sf /vendor/firmware/bcm4335c0.hcd ${D}${nonarch_libdir}/firmware/BCM4335C0.hcd

    # Hardware (OMX) video recording works on this device, so the camera app uses it: the plugin looks
    # for this file. It needs the OMX service's shim and the resource manager rc of the shared services.
    install -d ${D}${sysconfdir}/luneos
    touch ${D}${sysconfdir}/luneos/camera-hw-recording
}
FILES:${PN} += "${nonarch_libdir}/firmware/BCM4335C0.hcd ${sysconfdir}/luneos/camera-hw-recording"
