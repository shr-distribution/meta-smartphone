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
# used only by linux-samsung-*:do_deploy and it doesn't work with anything else
IMAGE_FSTYPES_forcevariable = "cpio.gz"

# temporary before it's fixed in oe-core
# http://patchwork.openembedded.org/patch/37453/
rootfs_ipk_do_rootfs[vardepsexclude] += "BUILDNAME"
rootfs_deb_do_rootfs[vardepsexclude] += "BUILDNAME"
rootfs_rpm_do_rootfs[vardepsexclude] += "BUILDNAME"
# http://patchwork.openembedded.org/patch/37455/
IMAGE_NAME[vardepsexclude] += "DATETIME"

LICENSE = "MIT"

inherit core-image
