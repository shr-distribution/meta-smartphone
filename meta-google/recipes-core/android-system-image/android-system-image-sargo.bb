require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sargo"

# Which system.img to put at /android: "device" is the sargo-specific Halium
# build, "gsi" is the device-agnostic halium_arm64 one. The device tarball is
# only fetched when one of these two knobs still asks for it, so the default
# configuration does not depend on it at all.
SARGO_ANDROID_SYSTEM ?= "gsi"

# Which vendor to use. "device" ships the sargo-specific vendor.img built
# alongside the old system image; "none" ships no vendor at all, so the phone's
# own /vendor partition is mounted instead. "none" is the Treble arrangement and
# the one that boots: generic Android 16 userspace over sargo's stock Android 12L
# vendor. mount_device_vendor() in initramfs-scripts-halium does the mounting;
# Halium's own mountroot cannot, because it only ever mounts a vendor from a
# shipped vendor.img and otherwise reads an fstab out of the Android image,
# which a device-agnostic GSI does not carry.
SARGO_ANDROID_VENDOR ?= "none"

# Device-specific build. Only reachable by setting SARGO_ANDROID_SYSTEM or
# SARGO_ANDROID_VENDOR back to "device"; nothing fetches it otherwise, which is
# just as well, because:
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
SARGO_DEVICE_PV = "20240301-3"

# PV tracks what this recipe actually ships, which is the GSI.
PV = "20260826-1"

# Device-agnostic halium_arm64 build.
#
# The 9.0 and 10.0 releases were tagged with the asset's own filename, so the
# tag and the asset were the same string and the URL just repeated it. Later
# releases are tagged by date and hold every generation at once, so the two are
# named separately - which is why SARGO_GSI_RELEASE exists.
#
# Which GSI generation to use is selectable, because moving sargo onto a modern
# Android base is the whole point of the GSI work (plan doc Phase 2). The
# published halium_arm64 builds are:
#
# Tagged with the asset's own filename - set SARGO_GSI_RELEASE to the tarball
# name to use one of these:
#
#   9.0   halium-luneos-9.0-20240228-1-halium_arm64.tar.bz2
#           sha256 7469662bb4d8440359dacee9edcae7d8ee7e9536ac8899a37c49c0f5ca1500c4
#   10.0  halium-luneos-10.0-20230130-1-halium_arm64.tar.bz2
#           sha256 not published as an asset; compute before first use
#   11.0  halium-luneos-11.0-20240219-1-halium_arm64.tar.bz2
#           sha256 355805c5bca803a386065c30408cfffb6024c7dfc34768a0ba61039747fe3976
#
# In the date-tagged halium-luneos-20260826 release, which carries every
# generation at once - set SARGO_GSI_RELEASE to that tag as well as the tarball:
#
#   9.0   halium-luneos-9.0-20260826-1-halium_arm64.tar.bz2
#           sha256 e61057c46694813707e6465b745f58afa3fb8a7a5b5e276316daef3ab335eadc
#   11.0  halium-luneos-11.0-20260826-1-halium_arm64.tar.bz2
#           sha256 fd6be3ca58b395e5214a517d1915d3e65a5aa755256cd19603019649c4e6eb71
#   13.0  halium-luneos-13.0-20260826-1-halium_arm64.tar.bz2
#           sha256 098f277ad55d41e84f05aeff95e5ea1131bc0516cb243611afa86e0611216c7b
#   14.0  halium-luneos-14.0-20260826-1-halium_arm64.tar.bz2
#           sha256 e31446a27296ebb81314834affd03671a1388fa1fdc837f737bd9304a18176b0
#   16.0  halium-luneos-16.0-20260826-1-halium_arm64.tar.bz2
#           sha256 8bcbb68b0bab9870d19560931a29c36e484873c797a32302048900e16cc147f3
#
# NB: the GSI's VNDK level has to match the vendor it runs against, and the
# default here now assumes the Treble arrangement - stock Android 12L flashed on
# the phone, its own /vendor mounted by mount_device_vendor(), and dynamic
# partition support in mount-android.sh. A 16.0 GSI on an Android 9 vendor will
# not boot. Whatever generation is pinned here, sargo.conf's
# PREFERRED_VERSION_android-headers-halium has to track it, not the vendor:
# libhybris gates its per-generation support on ANDROID_VERSION_MAJOR from
# those headers.
SARGO_GSI_RELEASE ?= "halium-luneos-20260826"
SARGO_GSI_TARBALL ?= "halium-luneos-16.0-20260826-1-halium_arm64.tar.bz2"
SARGO_GSI_SHA256 ?= "8bcbb68b0bab9870d19560931a29c36e484873c797a32302048900e16cc147f3"

SARGO_DEVICE_TARBALL = "halium-luneos-9.0-${SARGO_DEVICE_PV}-${MACHINE}.tar.bz2"
SARGO_DEVICE_URI = "https://github.com/webOS-ports/halium-images/releases/download/${SARGO_DEVICE_TARBALL}/${SARGO_DEVICE_TARBALL};name=device;subdir=device"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/${SARGO_GSI_RELEASE}/${SARGO_GSI_TARBALL};name=gsi;subdir=gsi"

# Fetch the device tarball only when something still consumes it. It is an
# unpublished asset (see above), so an unconditional entry made do_fetch fail
# for everyone without it already in DL_DIR - even though the default
# configuration never opens it.
SRC_URI += "${@d.getVar('SARGO_DEVICE_URI') if 'device' in (d.getVar('SARGO_ANDROID_SYSTEM'), d.getVar('SARGO_ANDROID_VENDOR')) else ''}"

# Checksum of the repacked device tarball, which is not byte-identical to
# whatever was originally intended for this version - the images inside are the
# shipped ones, but the bzip2 stream around them is new.
SRC_URI[device.sha256sum] = "c96011207034cab2f0a44b61a009b1c6d64a32c93beb6a9b87291d664bdd22de"
SRC_URI[gsi.sha256sum] = "${SARGO_GSI_SHA256}"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# Stage the selected pair where the .inc expects them. Done in do_install rather
# than after do_unpack because the .inc consumes ${UNPACKDIR}/system.img in
# place (mv to .sparse, simg2img, rm), so re-copying here keeps a re-run of
# do_install idempotent.
do_install:prepend() {
    cp ${UNPACKDIR}/${SARGO_ANDROID_SYSTEM}/system.img ${UNPACKDIR}/system.img
    # Same idea for the vndservicemanager the GSI tarball carries alongside
    # system.img. Only the GSI has one; the device build predates the problem
    # because Android 9's vndservicemanager tolerates a missing selinuxfs.
    rm -f ${UNPACKDIR}/vndservicemanager
    if [ -e ${UNPACKDIR}/${SARGO_ANDROID_SYSTEM}/vndservicemanager ]; then
        cp ${UNPACKDIR}/${SARGO_ANDROID_SYSTEM}/vndservicemanager ${UNPACKDIR}/vndservicemanager
    fi
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
