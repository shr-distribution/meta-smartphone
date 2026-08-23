require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sargo"

# Which system.img to put at /android: "device" is the sargo-specific Halium
# build, "gsi" is the device-agnostic halium_arm64 one. Both tarballs are
# always fetched so switching is a rebuild of this recipe, nothing more.
#
# This is Test A of the GSI migration: generic Android userspace on top of
# sargo's own vendor.img, so that a failure points at the system side rather
# than at vendor mounting. Test B drops vendor.img entirely and uses the
# device's stock /vendor - that is the actual Treble goal, and it also makes
# the recovered-tarball problem described below go away.
SARGO_ANDROID_SYSTEM ?= "gsi"

# Which vendor to use. "device" ships the sargo-specific vendor.img built
# alongside the old system image; "none" ships no vendor at all, so the phone's
# own /vendor partition is mounted instead - which is the actual Treble goal and
# what Test B checks. mount_device_vendor() in initramfs-scripts-halium does the
# mounting; Halium's own mountroot cannot, because it only ever mounts a vendor
# from a shipped vendor.img and otherwise reads an fstab out of the Android
# image, which a device-agnostic GSI does not carry.
SARGO_ANDROID_VENDOR ?= "device"

# Device-specific build.
#
# This version was never published to webOS-ports/halium-images - neither the
# release tag nor the asset exists there, so do_fetch could only ever 404. The
# obvious substitute, 20240307-1, does exist but its vendor.img is corrupt: the
# sparse header claims 196608 blocks in 17 chunks while only chunks 0-8 are real,
# the rest being type 0x0 with zero length, followed by ~12.5MB of trailing
# rubbish. simg2img gives up on it ("Failed to read sparse file") and do_install
# fails. The tarball itself is intact - bzip2 passes its CRCs - so the image went
# into that release already broken.
#
# The images restored here were recovered from a package that did ship and did
# boot, luneos-dev-package-sargo-testing-20240301114951.zip in the
# webOS-ports/luneos-testing 20240228 release, whose timestamp matches this
# version. They are taken from /var/lib/lxc/android in its rootfs, so they are
# already raw ext4 rather than sparse - do_install detects that and skips the
# simg2img conversion - and vendor.img passes a full e2fsck.
PV = "20240301-3"

# Device-agnostic halium_arm64 build.
#
# The 9.0 and 10.0 releases were tagged with the asset's own filename, so the
# tag and the asset were the same string and the URL just repeated it. The 11.0
# release is tagged by date instead and can hold more than one revision of the
# tarball, so the two are named separately now. SARGO_GSI_RELEASE defaults to
# the tarball name to keep the older pins resolving unchanged.
#
# Which GSI generation to use is selectable, because moving sargo onto a modern
# Android base is the whole point of the GSI work (plan doc Phase 2). The
# published halium_arm64 builds are:
#
#   9.0   halium-luneos-9.0-20240228-1-halium_arm64.tar.bz2
#           sha256 7469662bb4d8440359dacee9edcae7d8ee7e9536ac8899a37c49c0f5ca1500c4
#   10.0  halium-luneos-10.0-20230130-1-halium_arm64.tar.bz2
#           sha256 not published as an asset; compute before first use
#   11.0  halium-luneos-11.0-20240219-1-halium_arm64.tar.bz2
#           sha256 355805c5bca803a386065c30408cfffb6024c7dfc34768a0ba61039747fe3976
#
# NB: the GSI's VNDK level has to match the vendor it runs against. Pointing
# this at the 11.0 GSI while the phone still carries an Android 9 vendor will
# not boot - it needs stock Android 11 or 12 flashed first, and dynamic
# partition support in mount-android.sh, which is why these two land together.
SARGO_GSI_TARBALL ?= "halium-luneos-9.0-20240228-1-halium_arm64.tar.bz2"
SARGO_GSI_SHA256 ?= "7469662bb4d8440359dacee9edcae7d8ee7e9536ac8899a37c49c0f5ca1500c4"
SARGO_GSI_RELEASE ?= "${SARGO_GSI_TARBALL}"

SRC_URI = "\
    https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2;name=device;subdir=device \
    https://github.com/webOS-ports/halium-images/releases/download/${SARGO_GSI_RELEASE}/${SARGO_GSI_TARBALL};name=gsi;subdir=gsi \
"
# Checksum of the repacked device tarball, which is not byte-identical to
# whatever was originally intended for this version - the images inside are the
# shipped ones, but the bzip2 stream around them is new. It only resolves on a
# machine that already has it in DL_DIR until that asset is published.
SRC_URI[device.sha256sum] = "c96011207034cab2f0a44b61a009b1c6d64a32c93beb6a9b87291d664bdd22de"
SRC_URI[gsi.sha256sum] = "${SARGO_GSI_SHA256}"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# Stage the selected pair where the .inc expects them. Done in do_install rather
# than after do_unpack because the .inc consumes ${UNPACKDIR}/system.img in
# place (mv to .sparse, simg2img, rm), so re-copying here keeps a re-run of
# do_install idempotent.
do_install:prepend() {
    cp ${UNPACKDIR}/${SARGO_ANDROID_SYSTEM}/system.img ${UNPACKDIR}/system.img
    # Remove first so a re-run cannot leave a stale vendor.img behind when
    # switching to SARGO_ANDROID_VENDOR = "none".
    rm -f ${UNPACKDIR}/vendor.img
    if [ "${SARGO_ANDROID_VENDOR}" = "device" ]; then
        cp ${UNPACKDIR}/device/vendor.img ${UNPACKDIR}/vendor.img
    fi
}

# Without a vendor.img the .inc points /vendor at /system/vendor, which is empty
# in a GSI. The device vendor gets mounted at /android/vendor, so point there -
# the same place the vendor.img case uses.
do_install:append() {
    if [ "${SARGO_ANDROID_VENDOR}" != "device" ]; then
        ln -sf /android/vendor ${D}/vendor
    fi
}
