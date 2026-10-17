SUMMARY = "Initramfs image to boot from internal flash with initramfs"
DESCRIPTION = "Provides a minimal environment to bootstrap and run LuneOS"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_INSTALL = "busybox base-passwd bash util-linux-blkid"
IMAGE_INSTALL += "initramfs-scripts-simple"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
IMAGE_ROOTFS_EXTRA_SPACE = "0"
export IMAGE_BASENAME = "initramfs-simple-image"
IMAGE_NAME_SUFFIX ?= ""
IMAGE_LINGUAS = ""

IMAGE_FSTYPES:forcevariable ?= "cpio.gz"

# Populate /dev, so init has a console to talk to.
#
# USE_DEVFS defaults to "1", which means "leave /dev empty and let devtmpfs do
# it". That reasoning does not hold for an initramfs: CONFIG_DEVTMPFS_MOUNT only
# mounts devtmpfs on the path through prepare_namespace(), which the kernel does
# not take when it boots straight into an initramfs. So /dev stayed empty, the
# kernel logged
#
#   Warning: unable to open an initial console.
#
# and init ran with no stdin/stdout/stderr at all. It still ran - this is not
# why a boot fails - but nothing it echoed or any error it hit was visible, on
# any machine using this image. The first boot failure on the Mecha Comet was a
# bare "Attempted to kill init! exitcode=0x00000100" with not one line of
# init.sh output before it.
#
# device_table-minimal.txt is oe-core's own, and makedevs creates /dev/console
# (c 662 5:1) and /dev/null (c 666 1:3) from it. init_functions.sh mounts
# devtmpfs over the top almost immediately, so these two nodes only have to
# survive long enough for the kernel to wire up init's stdio.
USE_DEVFS = "0"
IMAGE_DEVICE_TABLES = "files/device_table-minimal.txt"

# We don't need depmod data here
KERNELDEPMODDEPEND = ""
USE_DEPMOD = "0"

inherit core-image
