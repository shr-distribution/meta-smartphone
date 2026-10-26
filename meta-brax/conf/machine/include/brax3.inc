#-----------------------------------------------------------------------------
# Shared by brax3.conf (BaseOS vendor) and brax3-iode.conf (iodéOS vendor).
#
# The BraX3, Brax Technologies, MediaTek Dimensity 6300 (MT6835). Same shape
# as q25.conf and mp01.conf: these machines exist to build the *boot images*.
# The rootfs comes from halium-arm64 and the GSI (Halium 16.0, halium_arm64)
# is chosen at install time.
#
#   MACHINE=brax3 bitbake luneos-bootimg-gki        boot-, init_boot- and
#   MACHINE=brax3-iode bitbake luneos-bootimg-gki   boot-...-debug.img per vendor
#
# Why two machines: the BaseOS and iodéOS vendor modules are one KMI (3,832
# shared symbols, 0 CRC differences, same module_layout), but their kernels
# report different releases - 5.15.209-android13-8-gdd511ad2a748 and
# 5.15.211-android13-8-g63db33e5daa0 - and the initramfs's busybox insmod needs
# the vermagic to match exactly. So the kernel is built once per vendor from
# the same tree with a different release string (BRAX3_VENDOR, read by the
# recipe), and each machine names its own images.
#
# Tier A on MediaTek's mgk kernel, like the MT6789 machines: the device keeps
# its stock vendor_boot and vendor_dlkm modules and we replace only the kernel,
# built from Brax's own published tree (see the recipe). Facts below are from
# Brax's BaseOS device tree (Brax3_BaseOs/android_device_brax_brax3,
# baseos-1.0), the iodéOS one (Brax3_IodeOs/device_brax_brax3, v7.9), the
# kernel tree and prebuilts, and the UBports port's boot images
# (Brax3_UbuntuTouchOs/brax3-ubports), all on git.os-source.co.
#-----------------------------------------------------------------------------

require conf/machine/include/arm/arch-arm64.inc
require conf/machine/include/meta-android-halium.inc

PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"
MACHINE_ESSENTIAL_EXTRA_RDEPENDS = ""
MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS = ""

GKI_BOOTIMG = "1"
GKI_KERNEL_PROVIDER = "linux-brax-brax3"

# BaseOS's BoardConfig.mk: BOARD_KERNEL_IMAGE_NAME := Image.lz4. (The UBports
# boot.img carries a gzip kernel, so this lk takes either.)
GKI_KERNEL_IMAGE_NAME = "Image.lz4"

# ThinLTO; KMI-neutral, measured 0/774 on the baseline (see the recipe).
KERNEL_LTO_THIN = "1"

# Touchscreen phone, no physical keyboard. NFC (sec-nfc in the device tree) and
# a FocalTech fingerprint reader (focaltech,fpspi; focaltech_fp.ko) are in the
# stock hardware description.
MACHINE_FEATURES = "alsa bluetooth gps usbgadget usbhost phone wifi vfat ext2 touchscreen nfc fingerprint"
MACHINE_FEATURES += "camera-rear camera-front"

# 720x1612. All four panel drivers the device ships (huayin ft8057s, huike and
# huashi ili9883, truly td4160c) declare HAC=720, VAC=1612 and a 67.93 x 152.09
# mm active area: 6.56" diagonal, 269 ppi. LineageOS ships density 280.
MACHINE_DISPLAY_WIDTH_PIXELS = "720"
MACHINE_DISPLAY_HEIGHT_PIXELS = "1612"
MACHINE_DISPLAY_ORIENTATION = "0"
MACHINE_DISPLAY_PPI = "269"

# Tracks the GSI generation, not the vendor (sargo.conf explains). Both vendors
# are LineageOS 23.2 on android-16.0.0_r4 (BaseOS 1.0, iodéOS v7.9) with
# BOARD_VNDK_VERSION := current and BOARD_SHIPPING_API_LEVEL := 33, i.e. A16
# vendors that need no VNDK snapshot from the GSI.
PREFERRED_VERSION_android-headers-halium = "16.0%"
PREFERRED_VERSION_pulseaudio-modules-droid = "17.0.103%"

VIRTUAL-RUNTIME_android-system-image = ""

IMAGE_FSTYPES += "tar.gz ext4 userdataimg"
IMAGE_ROOTFS_EXTRA_SPACE = "200000"
IMAGE_CLASSES += "userdataimg"
# boot and, beside it, init_boot (the fastboot package writes init_<partition>
# when meta-android-halium.inc names an init_boot image).
FASTBOOT_BOOT_PARTITIONS = "boot"

# The Mali driver patched to create GPU contexts inside a pid namespace, for
# Waydroid; see the kernel recipe. This reaches the device through
# MACHINE=brax3(-iode) bitbake luneos-dev-fastboot-package, whose rootfs is
# built for this machine - not through a separate halium-arm64 rootfs.
MACHINE_EXTRA_RRECOMMENDS += "linux-brax-brax3-vendor-dlkm-override"

# Boot images: header v4, and an init_boot partition - an Android-13-launch
# layout, the first MediaTek one here (q25 and mp01 have none). BaseOS:
# BOARD_BOOT_HEADER_VERSION = 4, BOARD_INIT_BOOT_HEADER_VERSION := 4,
# BOARD_INIT_BOOT_IMAGE_PARTITION_SIZE := 8388608 and init_boot in
# AB_OTA_PARTITIONS. The UBports images, which boot on hardware, have the same
# shape: boot.img v4 with kernel and a zero-length ramdisk, init_boot.img v4
# with the ramdisk alone. boot is 64 MiB.
ANDROID_BOOTIMG_HEADER_VERSION = "4"
ANDROID_BOOTIMG_INIT_BOOT = "1"

# init_boot is 8 MiB and our ramdisk image is 8,015,872 B (26 Oct 2026), so
# ~364 KiB of headroom: anything added to initramfs-android-image for this
# machine has to be weighed against it.
#
# The generic ramdisk is LZ4 legacy in the UBports init_boot.img (magic
# 02 21 4c 18), and BaseOS sets BOARD_RAMDISK_USE_LZ4 := true.
GKI_RAMDISK_COMPRESSION = "lz4-legacy"

# UNVERIFIED: left at the class default (0). q25 and mp01 copy the stock
# boot.img's os_version, but Brax publishes no stock boot image. Read the
# header of what is flashed (boot_a) before the first install and set
# (os_version << 11) | os_patch_level here; never lower than what is flashed.
# For reference, the UBports boot.img carries 13.0.0 / 2023-05 and boots.
#ANDROID_BOOTIMG_OS_VERSION = ""

# q25's padding, carried over defensively. The Helio G99 lk drops the first 20
# characters of the boot-image cmdline; whether the MT6835 lk does is
# UNMEASURED. If it does not, the first token becomes an unknown parameter the
# kernel ignores, which only loses log_buf_len. Everything LuneOS needs is in
# CONFIG_CMDLINE (luneos.cfg). cgroup_disable=net_prio: the MP01 deadlock,
# pre-empted as on q25 (q25.conf explains).
ANDROID_BOOTIMG_CMDLINE = "????????????????????log_buf_len=4M printk.devkmsg=on cgroup_disable=net_prio"

# 8250_mtk.ko is in the stock vendor_boot set, so ttyS0. UNVERIFIED as a usable
# console; no debug UART is known on this device.
SERIAL_CONSOLE = "115200 ttyS0"
