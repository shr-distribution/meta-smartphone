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

# Device-agnostic halium_arm64 build, from the same halium-9.0 tree, so the same
# VNDK level as the vendor.img above. Unlike the device tarball this one is
# published and its .sha256sum is served alongside it. It ships system.img only:
# a GSI has no business carrying a vendor.
GSI_PV = "20240228-1"

SRC_URI = "\
    https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2;name=device;subdir=device \
    https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${GSI_PV}-halium_arm64.tar.bz2/halium-luneos-9.0-${GSI_PV}-halium_arm64.tar.bz2;name=gsi;subdir=gsi \
"
# Checksum of the repacked device tarball, which is not byte-identical to
# whatever was originally intended for this version - the images inside are the
# shipped ones, but the bzip2 stream around them is new. It only resolves on a
# machine that already has it in DL_DIR until that asset is published.
SRC_URI[device.sha256sum] = "c96011207034cab2f0a44b61a009b1c6d64a32c93beb6a9b87291d664bdd22de"
SRC_URI[gsi.sha256sum] = "7469662bb4d8440359dacee9edcae7d8ee7e9536ac8899a37c49c0f5ca1500c4"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"

# Stage the selected pair where the .inc expects them. Done in do_install rather
# than after do_unpack because the .inc consumes ${UNPACKDIR}/system.img in
# place (mv to .sparse, simg2img, rm), so re-copying here keeps a re-run of
# do_install idempotent.
do_install:prepend() {
    cp ${UNPACKDIR}/${SARGO_ANDROID_SYSTEM}/system.img ${UNPACKDIR}/system.img
    cp ${UNPACKDIR}/device/vendor.img ${UNPACKDIR}/vendor.img
}
