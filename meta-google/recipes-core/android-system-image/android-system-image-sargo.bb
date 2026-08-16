require recipes-core/android-system-image/android-system-image.inc

COMPATIBLE_MACHINE = "sargo"

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
# simg2img conversion - and vendor.img passes a full e2fsck. system.img and
# vendor.img are kept as the pair that package shipped rather than mixed with
# 20240307-1's system.img, which is a different build (different UUID).
PV = "20240301-3"

SRC_URI = "https://github.com/webOS-ports/halium-images/releases/download/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2/halium-luneos-9.0-${PV}-${MACHINE}.tar.bz2"
# Checksum of the repacked tarball, which is not byte-identical to whatever was
# originally intended for this version - the images inside are the shipped ones,
# but the bzip2 stream around them is new.
SRC_URI[sha256sum] = "c96011207034cab2f0a44b61a009b1c6d64a32c93beb6a9b87291d664bdd22de"

ANDROID_SYSTEM_IMAGE_DESTNAME = "android-rootfs.img"
