#-----------------------------------------------------------------------------
#@TYPE: Machine
#@NAME: Asus Grouper (also known as Google Nexus 7)
#@DESCRIPTION: Machine configuration for the Asus Grouper
#-----------------------------------------------------------------------------

TARGET_ARCH = "arm"

MACHINE_CLASS = "grouper"

PREFERRED_PROVIDER_virtual/kernel ?= "linux-asus-grouper"
MACHINE_KERNEL_PR = "r1"

MACHINE_FEATURES = "kernel26 apm alsa bluetooth gps usbgadget usbhost wifi vfat ext2"
MACHINE_DISPLAY_WIDTH_PIXELS = "800"
MACHINE_DISPLAY_HEIGHT_PIXELS = "1280"
MACHINE_DISPLAY_ORIENTATION = "0"
MACHINE_DISPLAY_PPI = "233"

MACHINE_EXTRA_RDEPENDS = "asus-grouper-firmware"

PREFERRED_PROVIDER_virtual/egl ?= "libhybris"
PREFERRED_PROVIDER_virtual/mesa ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles1 ?= "libhybris"
PREFERRED_PROVIDER_virtual/libgles2 ?= "libhybris"

PREFERRED_PROVIDER_virtual/android-headers = "android-headers-grouper"
VIRTUAL-RUNTIME_android-system-image ?= "android-system-image-grouper"

PREFERRED_PROVIDER_virtual/xserver = "xserver-xorg"
XSERVER = " \
    xserver-xorg \
    xf86-input-evdev \
"

KERNEL_IMAGETYPE = "zImage"
IMAGE_FSTYPES += "tar.gz ext2 ext4"
IMAGE_ROOTFS_EXTRA_SPACE = "200000"

ANDROID_BOOTIMG_CMDLINE = "console=tty1"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x10008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x11000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x10f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x10000100"
ANDROID_BOOTIMG_BOOT_PARTITION = "/dev/mmcblk0p2"
ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL = "udev e2fsprogs-e2fsck e2fsprogs-resize2fs"

ANDROID_BOARD_BOOTIMAGE_PARTITION = "/dev/block/mmcblk0p2"
ANDROID_BOARD_SYSTEMIMAGE_PARTITION_SIZE = "681574400"
ANDROID_BOARD_FLASH_BLOCK_SIZE = "4096"

require conf/machine/include/tune-cortexa8.inc
