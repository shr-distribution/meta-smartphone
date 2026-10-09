require recipes-core/android-system-image/android-system-image-legacy-gsi.inc
require recipes-core/android-system-image/android-system-image-legacy-a9-services.inc

COMPATIBLE_MACHINE = "tenderloin-halium"

# The TouchPad runs a newer 32-bit GSI than the other legacy devices, which stay on the one
# android-system-image-legacy-gsi.inc names until their vendor images are published again: 20261008-1 gives
# the restored HAL1 CameraClient VIDEO_BUFFER_MODE_BUFFER_QUEUE, the only video mode Android's CameraSource
# uses, so its camera can record through the hardware encoder. 20261008-3, the published build, has that and
# adds the sphal libbinder/libui link for VNDK 30 vendors and five HIDL threads in minimediaservice (a HAL1
# takePicture() deadlocked without them); 20261008-1 was never published. The vendor image below re-derives
# with identical contents against it. Both are in the halium-luneos-20261005 release, which the date of the
# version does not name.
HALIUM_LUNEOS_GSI16_PV = "20261008-3"
HALIUM_LUNEOS_GSI16_SHA256 = "7795e2d7756707ea28a4dfc3d4f00406afe47339abe306168bc0035d2e6712c3"
HALIUM_LUNEOS_GSI16_RELEASE = "halium-luneos-20261005"

# The vendor image published with the GSI (tenderloin-halium-vendor.img in its release), made by the settings below
# against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one.
HALIUM_LEGACY_VENDOR_SHA256 = "3b82533d01a9fc79a3af55cb427692e5db46c3a241694d00eced5813acff3f1c"

HALIUM_LEGACY_DEVICE_URL = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-20210506-2-tenderloin.tar.bz2/halium-luneos-9.0-20210506-2-tenderloin.tar.bz2"
HALIUM_LEGACY_DEVICE_SHA256 = "800855aa74f752c774312c48143cc158d8d5e32a281f7f978851a0679350fe9b"

HALIUM_LEGACY_INIT_FILES = "init.tenderloin.rc init.tenderloin.power.rc ueventd.tenderloin.rc"

# The GSI is the plain halium_arm one: nothing of the old image goes into it.
HALIUM_LEGACY_GSI_AUTODETECT = "0"

# Qualcomm's HWC module (hwcomposer.msm8660.so) links the vendor's libhwui.so, which needs libft2.so. The GSI's
# LLNDK list names libft2.so, so the converter counts it as provided, but the vendor linker namespace of the
# container does not hand it out: "library "libft2.so" not found: needed by /vendor/lib/libhwui.so", the HWC
# module does not load, and the composer service exits with status 1 for ever (7 Oct 2026, as on sm-t520). A
# private copy from the old image fixes it.
HALIUM_LEGACY_EXTRA_LIBS += "libft2.so"

# The audio configuration of the Halium 9 build is in its /system/etc, which the GSI replaces: the audio
# HAL (audio.primary.tenderloin.so) looks for mixer_paths.xml in /vendor/etc and then /system/etc, and the
# only audio policy is the legacy audio_policy.conf, which pulseaudio-modules-droid reads from /vendor/etc
# (pulseaudio-distro-conf's droid-audio-config-gen hands it over when there is no XML). Without them there is
# no droid card, no pcm_output sink, and pulseaudio aborts and restarts for ever (7 Oct 2026).
HALIUM_LEGACY_COPY_FILES += "/system/etc/audio_policy.conf:/etc/audio_policy.conf \
    /system/etc/mixer_paths.xml:/etc/mixer_paths.xml"

# The TouchPad is a Wi-Fi tablet with no camera HAL, no modem and no vendor OMX codecs in its Halium 9 build,
# so of the shared services (android-system-image-legacy-a9-services.inc) it takes the graphics ones, the
# crash_dump stand-in and the shim, not the RIL set, and the OMX set for gst-droid and the codecs (below). Its Bluetooth (a CSR BlueCore
# on a UART, spoken in BCSP) has no Android HAL; the host's tenderloin-bluetooth-utilities attaches it.

# The front camera (Aptina MT9M113 on the 3.4 kernel's legacy msm_camera driver) comes from
# tenderloin-camera.tar.xz instead: the tenderloin-common HAL1 module built in the Android 9 tree, the
# Android 9 provider and device implementations that serve it, and the HP blobs it loads (see BUILD.md in
# the tarball; all of it built for ARMv7, which the shared A9 binaries are not). Its rc is the TouchPad's own, as there is no camera
# daemon to wait for, and the old manifest has no camera entry, so a VINTF fragment declares the provider.
# ueventd-msm-camera.rc places the camera nodes where liboemcamera opens them.
SRC_URI += " \
    file://tenderloin-camera.tar.xz;subdir=camera \
    file://tenderloin-camera-provider.rc \
    file://camera-provider-start.sh \
    file://ueventd-msm-camera.rc \
    file://android.hardware.camera.provider@2.4.xml \
    file://tenderloin-no-installd.rc \
"
TENDERLOIN_CAMERA = "camera/tenderloin-camera"
HALIUM_LEGACY_EXTRA_FILES += " \
    tenderloin-no-installd.rc:/etc/init/tenderloin-no-installd.rc:644 \
    ${TENDERLOIN_CAMERA}/bin/hw/android.hardware.camera.provider@2.4-service:/bin/hw/android.hardware.camera.provider@2.4-service:755 \
    tenderloin-camera-provider.rc:/etc/init/android.hardware.camera.provider@2.4-service.rc:644 \
    camera-provider-start.sh:/bin/camera-provider-start.sh:755 \
    ${TENDERLOIN_CAMERA}/lib/liboemcamera_reserve.so:/lib/liboemcamera_reserve.so:644 \
    android.hardware.camera.provider@2.4.xml:/etc/vintf/manifest/android.hardware.camera.provider@2.4.xml:644 \
    ${TENDERLOIN_CAMERA}/lib/hw/camera.msm8660.so:/lib/hw/camera.msm8660.so:644 \
    ${TENDERLOIN_CAMERA}/lib/hw/android.hardware.camera.provider@2.4-impl.so:/lib/hw/android.hardware.camera.provider@2.4-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/camera.device@1.0-impl.so:/lib/camera.device@1.0-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/camera.device@3.2-impl.so:/lib/camera.device@3.2-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/camera.device@3.3-impl.so:/lib/camera.device@3.3-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/camera.device@3.4-impl.so:/lib/camera.device@3.4-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/camera.device@3.4-external-impl.so:/lib/camera.device@3.4-external-impl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/libbinder_shim.so:/lib/libbinder_shim.so:644 \
    ${TENDERLOIN_CAMERA}/lib/liboemcamera.so:/lib/liboemcamera.so:644 \
    ${TENDERLOIN_CAMERA}/lib/libmmjpeg.so:/lib/libmmjpeg.so:644 \
    ${TENDERLOIN_CAMERA}/lib/libmmipl.so:/lib/libmmipl.so:644 \
    ${TENDERLOIN_CAMERA}/lib/libgemini.so:/lib/libgemini.so:644 \
"
HALIUM_LEGACY_UEVENTD_EXTRA = "ueventd-msm-camera.rc"

# The OMX service, which the Halium 9 build has no hardware codecs for (they come from tenderloin-media below):
# the camera app reaches the camera through gst-droid, whose plugin gst-droid-gate.sh only puts on the
# GStreamer path once android.hardware.media.omx@1.0::IOmxStore answers, and the old manifest already declares
# the service hwbinder. libdroidmedia then also needs the Codec2 software
# store (media.swcodec), which loads gralloc.msm8660.so in the sphal namespace; that needs libbinder.so and
# libui.so from the VNDK, which only a GSI whose linkerconfig hands them to sphal for VNDK 28 vendors does.
# Without it media.swcodec aborts, and gst-plugin-scanner, with it surface-manager, hangs on boot.
HALIUM_LEGACY_EXTRA_FILES += "${HALIUM_LEGACY_A9_OMX}"

# The hardware video encoders and decoders (Qualcomm legacy vidc, /dev/msm_vidc_enc and /dev/msm_vidc_dec) for
# the OMX service above, built for ARMv7 in the Android 9 tree (see BUILD.md in the tarball), and the
# media_codecs.xml that declares them.
SRC_URI += "file://tenderloin-media.tar.xz;subdir=media"
TENDERLOIN_MEDIA = "media/tenderloin-media"
HALIUM_LEGACY_EXTRA_FILES += " \
    ${TENDERLOIN_MEDIA}/lib/libOmxCore.so:/lib/libOmxCore.so:644 \
    ${TENDERLOIN_MEDIA}/lib/libOmxVenc.so:/lib/libOmxVenc.so:644 \
    ${TENDERLOIN_MEDIA}/lib/libOmxVdec.so:/lib/libOmxVdec.so:644 \
    ${TENDERLOIN_MEDIA}/lib/libdivxdrmdecrypt.so:/lib/libdivxdrmdecrypt.so:644 \
    ${TENDERLOIN_MEDIA}/lib/libstagefrighthw.so:/lib/libstagefrighthw.so:644 \
    ${TENDERLOIN_MEDIA}/lib/libc2dcolorconvert.so:/lib/libc2dcolorconvert.so:644 \
    ${TENDERLOIN_MEDIA}/etc/media_codecs.xml:/etc/media_codecs.xml:644 \
"

# Hardware (OMX) video recording works on this device, so the camera app uses it: the plugin looks for this
# file. It needs the encoders above and the GSI's buffer-queue video mode.
do_install:append() {
    install -d ${D}${sysconfdir}/luneos
    touch ${D}${sysconfdir}/luneos/camera-hw-recording
}
FILES:${PN} += "${sysconfdir}/luneos/camera-hw-recording"

# What the TouchPad's init script does that the host has already done, or must keep to itself: it
# activates the LVM volume groups (the host's initramfs has), mounts the webOS /boot partition and
# deletes moboot.next, the next-boot marker (the host owns the boot loader's choice), and has a service that
# reboots into recovery on a shutdown property. None of that belongs in the container; ts_srv, the
# touchscreen's userspace daemon, does.
HALIUM_LEGACY_SKIP_SERVICES += "lvm reboot_recovery"
HALIUM_LEGACY_SKIP_COMMANDS = " \
    start\s+lvm \
    mount\s+ext3\s+/dev/block/mmcblk0p13 \
    moboot\.next \
"
