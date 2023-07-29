SUMMARY = "Initramfs image to boot from internal flash with initramfs, using uboot"
DESCRIPTION = "Provides a minimal environment to bootstrap and run LuneOS from U-Boot"

require initramfs-simple-image.inc
IMAGE_FSTYPES:forcevariable = "cpio.gz.u-boot"
export IMAGE_BASENAME = "initramfs-uboot-image"
