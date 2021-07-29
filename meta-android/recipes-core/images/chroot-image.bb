#
# Simple image to chroot into an existing rootfs which can not be entered directly from
# the kernel. In this case the initscript searches for a rootfs.ext2 file on the internal
# flash disk.
#

IMAGE_INSTALL = "busybox base-passwd chroot-script android-tools"
IMAGE_FEATURES = ""
IMAGE_ROOTFS_SIZE = "8192"
export IMAGE_BASENAME = "chroot-image"
IMAGE_LINGUAS = ""
# used only by linux-samsung-*:do_deploy and it doesn't work with anything else
IMAGE_FSTYPES:forcevariable = "cpio.gz"

LICENSE = "MIT"

inherit core-image
