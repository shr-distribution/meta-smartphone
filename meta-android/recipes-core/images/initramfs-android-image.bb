SUMMARY = "Initramfs image to boot from internal flash of several Android based devices"
DESCRIPTION = "Provides a minimal environment to bootstrap and run a linux system on a \
Android based device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_INSTALL = "busybox base-passwd initramfs-boot-android android-tools bash"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
export IMAGE_BASENAME = "initramfs-android-image"
IMAGE_LINGUAS = ""

# NOTE we must use cpio.gz here as this is what mkbootimg requires
IMAGE_FSTYPES_forcevariable = "cpio.gz"

# We don't need depmod data here
KERNELDEPMODDEPEND = ""
USE_DEPMOD = "0"

inherit core-image
