require recipes-core/android-system-image/android-system-image-legacy-gsi.inc

COMPATIBLE_MACHINE = "^bullhead-halium$"

# The vendor image published with the GSI (bullhead-halium-vendor.img in its release), made by the settings
# below against that GSI. See android-system-image-legacy-gsi.inc for how to make a new one.
HALIUM_LEGACY_VENDOR_SHA256 = "f62c1ad3540dc385c70a0ffe9dbdcf5b9b54ad03fc7da2d5f1f869e1dcecd983"

# The vendor is the one of the LineageOS 21 build of github.com/nexus5x-dev
# (device_lge_bullhead, kernel_lge_bullhead and vendor_lge_bullhead, branch lineage-21.0),
# taken from the release asset lineage-21.0-20240911-UNOFFICIAL-bullhead.zip of
# nexus5x-dev/OTA (sha256 1ed3d57e9d41695349c3401a0581dc299655d238cfdb6b2b4d99d49755df6065).
# That build is monolithic: no vendor partition, the vendor under /system/vendor, the device
# init scripts and fstab at the root of a system-as-root image, which is the layout
# halium-legacy-vendor converts. The "device" tarball is its system.img, unpacked from the
# block OTA (system.new.dat.br + system.transfer.list) and packed unchanged. It is not
# published: it is only needed to make a new vendor image (see android-system-image-legacy-gsi.inc).
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

# The GSI is the plain halium_arm64 one, so the vendor image can be published next to it: the
# files the vendor opens under /system/etc go to /vendor/etc instead, and the paths that name
# them are changed in the old image before the conversion (bullhead_vendor_etc_paths below):
#  - data/{dsi,netmgr,qmi}_config.xml: netmgrd, qmuxd and libdsi_netctrl.so spell
#    /system/etc/data/<file> in their code; "/vendor" is as long as "/system", so the string is
#    changed in place
#  - qcril.db: init.bullhead.rc copies it from /system/etc to /data/misc/radio, where the RIL
#    opens it
#  - sec_config: irsc_util's argument, the IPC router security rules, which init.bullhead.rc
#    names as /etc/sec_config; zz-bullhead-irsc.rc redefines the service
# GPS (gps.conf, izat.conf, sap.conf, flp.conf, lowi.conf, opened as /etc/<file>) is left out:
# GPS will run without its configuration. The autodetection is off because it also picks the
# GSI's own business (dirty-image-objects, swcodec/ld.config.txt).
HALIUM_LEGACY_GSI_AUTODETECT = "0"
BULLHEAD_ETC_DATA = "dsi_config.xml netmgr_config.xml qmi_config.xml"
HALIUM_LEGACY_COPY_FILES += " \
    ${@' '.join('/system/etc/data/%s:/etc/data/%s' % (f, f) for f in d.getVar('BULLHEAD_ETC_DATA').split())} \
    /system/etc/qcril.db:/etc/qcril.db \
    /system/etc/sec_config:/etc/sec_config \
"
SRC_URI += "file://zz-bullhead-irsc.rc"
HALIUM_LEGACY_EXTRA_FILES += "zz-bullhead-irsc.rc:/etc/init/zz-bullhead-irsc.rc:644"

# <path in the old image> <old string> <new string>, the strings of equal length. The bytes are
# changed in the file's own blocks of the unpacked device image, so its inode, owner, mode and
# SELinux label stay as they are. Runs only when the vendor image is derived.
BULLHEAD_VENDOR_PATH_EDITS = " \
    /system/vendor/bin/netmgrd:/system/etc/data/:/vendor/etc/data/ \
    /system/vendor/bin/qmuxd:/system/etc/data/:/vendor/etc/data/ \
    /system/vendor/lib/libdsi_netctrl.so:/system/etc/data/:/vendor/etc/data/ \
    /system/vendor/lib64/libdsi_netctrl.so:/system/etc/data/:/vendor/etc/data/ \
    /init.bullhead.rc:copy\x20/system/etc/qcril.db:copy\x20/vendor/etc/qcril.db \
"

python bullhead_vendor_etc_paths() {
    import os
    import subprocess

    img = os.path.join(d.getVar("UNPACKDIR"), "device", "system.img")
    with open(img, "rb") as f:
        if f.read(4) == b"\x3a\xff\x26\xed":
            bb.fatal("%s is sparse; the path edits expect the raw image" % img)

    def debugfs(request):
        return subprocess.run(["debugfs", "-R", request, img], capture_output=True,
                              check=True).stdout

    bs = int(subprocess.run(["dumpe2fs", "-h", img], capture_output=True, text=True, check=True)
             .stdout.split("Block size:")[1].split()[0])
    for edit in d.getVar("BULLHEAD_VENDOR_PATH_EDITS").split():
        path, old, new = edit.split(":")
        old = old.replace("\\x20", " ").encode()
        new = new.replace("\\x20", " ").encode()
        if len(old) != len(new):
            bb.fatal("%s: %r and %r differ in length" % (path, old, new))
        data = debugfs("cat %s" % path)
        if not data:
            bb.fatal("%s is not in the device image" % path)
        offsets = []
        start = data.find(old)
        while start >= 0:
            offsets.append(start)
            start = data.find(old, start + 1)
        if not offsets:
            if new in data:
                bb.note("%s: already reads %s" % (path, new.decode()))
                continue
            bb.fatal("%s: %s not found" % (path, old.decode()))
        # The physical block of each logical block the strings touch. The image's files can have
        # holes (libdsi_netctrl.so does), so the block list is not the file laid end to end; a
        # string is never in a hole, as a hole reads as zeros.
        phys = {}
        for lblk in sorted({(off + i) // bs for off in offsets for i in range(len(new))}):
            phys[lblk] = int(debugfs("bmap %s %d" % (path, lblk)).split()[0])
            if not phys[lblk]:
                bb.fatal("%s: logical block %d is a hole" % (path, lblk))
        with open(img, "r+b") as f:
            for off in offsets:
                for i in range(len(new)):
                    f.seek(phys[(off + i) // bs] * bs + (off + i) % bs)
                    f.write(new[i:i + 1])
        if debugfs("cat %s" % path) != data.replace(old, new):
            bb.fatal("%s: the edit did not read back" % path)
        bb.note("%s: %s -> %s (%d times)" % (path, old.decode(), new.decode(), len(offsets)))
}
do_halium_legacy_vendor[prefuncs] += "bullhead_vendor_etc_paths"

# Without property_overrides_split_enabled LineageOS puts every vendor property in
# /system/build.prop (ro.board.platform, vendor.rild.libpath, the radio, audio, camera and
# Bluetooth tuning), and the GSI replaces that file. These prefixes carry them over into the
# vendor build.prop, in addition to the default list.
HALIUM_LEGACY_SYSTEM_PROP_PREFIXES += "ro.board. ro.hardware. ro.opengles. ro.sf. ro.qc. qcom. ro.bt. \
    bluetooth.enable_timeout_ms persist.camera. ro.camera. persist.qcril. persist.dbg. vendor. vidc. \
    audio_hal. ro.min_freq_ ro.vendor. ro.frp. ril. ro.audio. persist.speaker."
