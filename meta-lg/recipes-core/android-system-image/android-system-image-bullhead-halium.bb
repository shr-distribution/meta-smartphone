require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

COMPATIBLE_MACHINE = "^bullhead-halium$"

# NOT BUILDABLE YET: this needs two changes to lib/halium/legacy_vendor.py that are not in
# the layer (see ~/claude-scratch/bullhead/STATUS.md): HALIUM_LEGACY_VNDK = "" for a vendor
# built without VNDK, and the 64-bit library directories (/system/lib64, /system/lib64/hw).
# With the module as it is, the build stops in vndk_sonames() and would not move the 64-bit
# libraries.
#
# The vendor is the one of the LineageOS 21 build of github.com/nexus5x-dev
# (device_lge_bullhead, kernel_lge_bullhead and vendor_lge_bullhead, branch lineage-21.0),
# taken from the release asset lineage-21.0-20240911-UNOFFICIAL-bullhead.zip of
# nexus5x-dev/OTA (sha256 1ed3d57e9d41695349c3401a0581dc299655d238cfdb6b2b4d99d49755df6065).
# That build is monolithic: no vendor partition, the vendor under /system/vendor, the device
# init scripts and fstab at the root of a system-as-root image, which is the layout
# halium-legacy-vendor converts. The "device" tarball is its system.img, unpacked from the
# block OTA (system.new.dat.br + system.transfer.list) and packed unchanged.
# TODO: publish to webOS-ports/halium-images and use an https URL.
BULLHEAD_HALIUM_DEVICE_TARBALL ?= "file:///home/herrie/claude-scratch/bullhead/out/bullhead-halium-device-lineage-21.0-20240911.tar.bz2"
HALIUM_LEGACY_DEVICE_URL = "${BULLHEAD_HALIUM_DEVICE_TARBALL}"
HALIUM_LEGACY_DEVICE_SHA256 = "c0b590bb080c512df48463238ed87fe11bc6accedd64733081de0ea3ab6d507f"

# The arm64 GSI, not the 32-bit one android-system-image-legacy-gsi.inc picks: the vendor is
# arm64 with a 32-bit second architecture (camera HAL and its daemon are 32-bit only). This is
# the build android-system-image-tissot and halium-luneos-gsi-16.inc use; 20261007-1 is the first
# with /persist, /efs and the /firmware link of its own, so the plain GSI goes with the vendor.
HALIUM_LUNEOS_GSI16_ARCH = "halium_arm64"
HALIUM_LUNEOS_GSI16_PV = "20261007-1"
HALIUM_LUNEOS_GSI16_SHA256 = "89d73b83a161e9ef5270aab803b4120519f02144aa8df631fac0a86eaa1a84ea"

# Built with RELEASE_DEPRECATE_VNDK (the ap2a release config of LineageOS 21, Android 14 QPR2):
# no ro.vndk.version, its own copies of libbinder, libhidlbase and the rest in /vendor/lib and
# /vendor/lib64, and /vendor/etc/selinux/plat_sepolicy_vers.txt says 202404. The GSI's
# linkerconfig gives such a vendor the LLNDK only, and no VNDK APEX is involved.
HALIUM_LEGACY_VNDK = ""

# The root of the image holds init.bullhead{,.diag,.fp,.misc,.ramdump,.sensorhub,.usb}.rc and
# fstab.bullhead; the vendor has its own /vendor/etc/ueventd.rc and every daemon in /vendor/bin.
# init.bullhead.usb.rc stays out, as for hammerhead: the container must not write the USB gadget
# the host's adbd owns.
HALIUM_LEGACY_INIT_FILES = "init.bullhead.rc init.bullhead.diag.rc init.bullhead.fp.rc \
    init.bullhead.sensorhub.rc init.bullhead.ramdump.rc init.bullhead.misc.rc"

# swapon_all: zram is the host's business. The bind mount of /system/etc/swcodec/ld.config.txt
# over the media.swcodec APEX's is an Android 14 fix for that APEX; the Android 16 GSI has its
# own APEX and no such file.
HALIUM_LEGACY_SKIP_COMMANDS = "^\s*swapon_all\b swcodec/ld\.config\.txt"

# The vendor's manifest already declares the composer, allocator, camera provider, OMX and
# radio as hwbinder services with their own binaries in /vendor/bin/hw: nothing to rewrite.
HALIUM_LEGACY_BINDERIZED_HALS = ""

# Without a vendor partition LineageOS installs part of the vendor's configuration in
# /system/etc (device.mk copies the audio files to TARGET_COPY_OUT_SYSTEM). The audio HAL looks
# in /odm/etc, /vendor/etc and /system/etc (strings of audio.primary.msm8992.so), so they go to
# /vendor/etc, as do the codec lists and the ACDB calibration that libacdbloader opens through
# /etc/acdbdata (linked to /vendor/etc/acdbdata by android-system-image-legacy-gsi.inc).
BULLHEAD_ACDB = "MTP_Bluetooth_cal MTP_General_cal MTP_Global_cal MTP_Handset_cal MTP_Hdmi_cal \
    MTP_Headset_cal MTP_Speaker_cal"
BULLHEAD_ETC = "audio_platform_info.xml audio_policy_configuration.xml audio_policy_volumes_drc.xml \
    default_volume_tables.xml r_submix_audio_policy_configuration.xml usb_audio_policy_configuration.xml \
    sound_trigger_mixer_paths.xml sound_trigger_platform_info.xml media_codecs.xml \
    media_codecs_google_audio.xml media_codecs_google_telephony.xml media_codecs_google_video.xml \
    media_codecs_performance.xml"
HALIUM_LEGACY_COPY_FILES = " \
    ${@' '.join('/system/etc/%s:/etc/%s' % (f, f) for f in d.getVar('BULLHEAD_ETC').split())} \
    ${@' '.join('/system/etc/acdbdata/MTP/%s.acdb:/etc/acdbdata/MTP/%s.acdb' % (n, n) for n in d.getVar('BULLHEAD_ACDB').split())} \
"

# Files the vendor opens by a /system/etc or /etc path, so they have to be in the GSI:
#  - data/{dsi,netmgr,qmi}_config.xml: netmgrd and the data services (literal /system/etc paths)
#  - qcril.db: init.bullhead.rc copies it to /data/misc/radio
#  - sec_config: irsc_util's argument in init.bullhead.rc, the IPC router security config
# GPS (gps.conf, izat.conf, sap.conf, flp.conf, lowi.conf) is left out for now: the GSI has 15
# free inodes and these take 8 with /persist and /firmware (7 left). The autodetection is off because it
# also picks the GSI's own business (dirty-image-objects, swcodec/ld.config.txt).
HALIUM_LEGACY_GSI_AUTODETECT = "0"
HALIUM_LEGACY_GSI_FILES = "/system/etc/sec_config /system/etc/qcril.db /system/etc/data/dsi_config.xml \
    /system/etc/data/netmgr_config.xml /system/etc/data/qmi_config.xml"

# Without property_overrides_split_enabled LineageOS puts every vendor property in
# /system/build.prop (ro.board.platform, vendor.rild.libpath, the radio, audio, camera and
# Bluetooth tuning), and the GSI replaces that file. These prefixes carry them over into the
# vendor build.prop, in addition to the default list.
HALIUM_LEGACY_SYSTEM_PROP_PREFIXES += "ro.board. ro.hardware. ro.opengles. ro.sf. ro.qc. qcom. ro.bt. \
    bluetooth.enable_timeout_ms persist.camera. ro.camera. persist.qcril. persist.dbg. vendor. vidc. \
    audio_hal. ro.min_freq_ ro.vendor. ro.frp. ril. ro.audio. persist.speaker."
