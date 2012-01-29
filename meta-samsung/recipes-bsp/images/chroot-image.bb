#
# Simple image to chroot into an existing rootfs which can not be entered directly from
# the kernel. In this case the initscript searches for a rootfs.ext2 file on the internal
# flash disk.
#

IMAGE_INSTALL = "busybox base-passwd chroot-script"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
export IMAGE_BASENAME = "chroot-image"
IMAGE_LINGUAS = ""
IMAGE_FSTYPES = "cpio.gz"

LICENSE = "MIT"

inherit core-image
