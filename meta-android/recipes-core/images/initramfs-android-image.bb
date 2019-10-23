SUMMARY = "Initramfs image to boot from internal flash of several Android based devices"
DESCRIPTION = "Provides a minimal environment to bootstrap and run a linux system on a \
Android based device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL ?= ""

VIRTUAL-RUNTIME_android-initramfs-scripts ?= "initramfs-scripts-android"

IMAGE_INSTALL = "busybox busybox-mdev base-passwd android-tools bash"
IMAGE_INSTALL += "${VIRTUAL-RUNTIME_android-initramfs-scripts}"
IMAGE_INSTALL += "${ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL}"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
export IMAGE_BASENAME = "initramfs-android-image"
IMAGE_NAME_SUFFIX ?= ""
IMAGE_LINGUAS = ""

# NOTE we must use cpio.gz here as this is what mkbootimg requires
IMAGE_FSTYPES:forcevariable = "cpio.gz"

# We don't need depmod data here
KERNELDEPMODDEPEND = ""
USE_DEPMOD = "0"

inherit core-image nopackages
