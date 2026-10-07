inherit linux-mainline-8916

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^a3-2015$"

# parameters for the fastboot image
inherit kernel_android

# From postmarketOS' device-samsung-a3 deviceinfo, which gives them relative to
# deviceinfo_flash_offset_base="0x80000000": kernel 0x00080000,
# ramdisk 0x02000000, second 0x00f00000, tags 0x01e00000.
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80080000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x82000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x81e00000"

# The MSM8916 UART is on the headset jack on this device; earlycon is what
# makes the difference between a black screen and a diagnosable one.
ANDROID_BOOTIMG_CMDLINE = "earlycon console=ttyMSM0,115200"

# The ramdisk is xz, not gzip - see initramfs-android-image.bbappend for why.
# Set HERE and not in a3-2015.conf: kernel_android.bbclass assigns INITRAMFS_NAME
# unconditionally with "=", so anything the machine conf sets is overwritten when
# the class is inherited. This has to land after that inherit.
INITRAMFS_NAME = "initramfs-android-image-${MACHINE}.cpio.xz"

do_deploy[depends] += " lk2nd-msm8916:do_deploy"

SRC_URI += " \
    file://defconfig \
"
