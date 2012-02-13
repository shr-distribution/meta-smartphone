#
# initramfs for kernel development
#

KERNEL_MODULES = " \
  kernel-module-snd-soc-neo1973-wm8753 \
  kernel-module-snd-soc-dfbmcs320 \
  kernel-module-snd-soc-wm8753 \
  kernel-module-snd-soc-s3c24xx \
  kernel-module-g-ether \
  kernel-module-ar6000 \
  kernel-module-btusb \
  kernel-module-hidp \
  kernel-module-bnep \
  kernel-module-rfcomm \
"
IMAGE_INSTALL = "busybox base-passwd initscripts ${KERNEL_MODULES} \
  task-base-wifi \ 
  task-boot \
  glibc-utils \
  sysvinit \
  sysvinit-inittab \
  glibc-charmap-utf-8 \
  netbase \
  sysfsutils \
  modutils-initscripts \
  module-init-tools-depmod \
  util-linux-fdisk \
"
IMAGE_FEATURES = "package-management ssh-server-openssh"
IMAGE_ROOTFS_SIZE = "8192"
export IMAGE_BASENAME = "initramfs-image"
IMAGE_LINGUAS = ""
IMAGE_FSTYPES = "cpio.gz"

LICENSE = "MIT"

inherit core-image
