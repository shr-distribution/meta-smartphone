#-----------------------------------------------------------------------------
# Everything the Unihertz MT6771 machines share.
#
# All three Titans - Titan, Titan Pocket, Titan Slim - are the same SoC
# (MediaTek Helio P60/P70 = MT6771), the same arm64 4.14 ALPS BSP family, the
# same boot geometry and the same install story. What differs per machine is
# the kernel branch, the defconfig, the dtb, the panel and the os_version
# stamped into the boot header. Those live in the machine .conf; everything
# else is here.
#
# These machines exist to build the *boot image*, the way halium-arm64.conf
# says a device machine should: the rootfs comes from MACHINE=halium-arm64 and
# the GSI is chosen at install time from the device's vendor API level. Same
# arrangement as sargo, bluejay, surya and q25.
#
#   MACHINE=titanpocket bitbake initramfs-android-image      the Halium initramfs
#   MACHINE=titanpocket bitbake linux-unihertz-titanpocket   boot.img (kernel + initrd)
#
# Tier B: MT6771 predates GKI by years, there is no KMI to preserve and no
# stock vendor module set to keep CRC-compatible, so the kernel is built whole
# from source and the config is ours to set. The KMI-poison list that cost
# bluejay and q25 a build cycle each - SYSVIPC, USER_NS, IPC_NS - does not
# apply here at all; see files/luneos.cfg, where they are simply =y.
#-----------------------------------------------------------------------------

require conf/machine/include/arm/arch-arm64.inc
require conf/machine/include/meta-android-halium.inc

# No rootfs is built for these machines (see above), so nothing is pulled in
# for one. Same arrangement as sargo.conf, surya.conf and bluejay.conf.
MACHINE_ESSENTIAL_EXTRA_RDEPENDS = ""
MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS = ""

# No GSI baked in: rootfs.img and android-rootfs.img sit side by side in the
# Linux partition and the GSI is picked at install time from the vendor API
# level. Both vendor levels in this family are covered by the shipping generic
# tarball with no VNDK work - the 16.0 halium_arm64 tree carries
# prebuilts/vndk v27-v34, PRODUCT_EXTRA_VNDK_VERSIONS := 27 .. 34 and an
# apex_vndk block for each, and com.android.vndk.v29/v30 are present in the
# build that went into halium-luneos-16.0-20260910-1-halium_arm64.tar.bz2.
#
#   Titan         Android 10 vendor -> VNDK 29
#   Titan Pocket  Android 11 vendor -> VNDK 30
#   Titan Slim    Android 11 vendor -> VNDK 30
VIRTUAL-RUNTIME_android-system-image = ""

# Tracks the GSI generation, not the vendor: libhybris gates its
# per-generation support on ANDROID_VERSION_MAJOR, which comes from this
# package. sargo.conf spells out the failure modes at length.
PREFERRED_VERSION_android-headers-halium = "16.0%"

# Treble-era GSI, so the modern droid modules rather than the jb2q ones the
# halium include defaults to.
PREFERRED_VERSION_pulseaudio-modules-droid = "17.0.103%"

# Deliberately no XSERVER: these machines build no rootfs, and the stack is
# hwcomposer-based, which xf86-video-fbdev does not serve.

IMAGE_FSTYPES += "tar.gz"
IMAGE_ROOTFS_EXTRA_SPACE = "200000"

# The dtb rides in the v2 boot header's own section as a prebuilt AOSP dt_table
# blob (see the kernel recipes), so the kernel slot holds the kernel alone -
# no appended dtb, unlike athena's Image.gz-dtb.
#
# GZIPPED, and that is not a preference. UBports' make-bootimage.sh autodetects
# with IMAGE_LIST="Image.gz Image" - gzip first - and neither Titan deviceinfo
# sets deviceinfo_kernel_image_name to override it. The Pocket/Slim deviceinfos
# settle it beyond doubt for the stock kernel too: they carry the stock image
# as a raw "TitanPocket-Image" and build.sh does
#     gzip -c "$deviceinfo_bootimg_prebuilt_kernel" > .../boot/Image.gz
# before packing, so what MediaTek lk has always been handed on these devices
# is the compressed one. Same conclusion q25.conf reaches for MT6789
# ("MediaTek lk takes the gzip one").
KERNEL_IMAGETYPE = "Image.gz"

# MTK 4.14 registers ttyMT via CONFIG_MTK_SERIAL, not the 8250 driver the
# GKI-era MediaTek machines use (q25 is ttyS0). UNVERIFIED as a usable console:
# no Titan exposes a debug UART without opening it. The real bring-up channel
# on this family is expdb over the preloader with mtkclient - see the README.
SERIAL_CONSOLE = "921600 ttyMT0"

# MTK, but unlike the GKI-era MediaTek devices these expose by-name symlinks
# under the platform path as well as /dev/disk/by-partlabel. Confirmed from the
# UBports udev rules for this family, which key on
# /dev/block/platform/bootdevice/by-name/{boot,recovery,seccfg,nvram,...}.
ANDROID_BOARD_BOOTIMAGE_PARTITION = "/dev/block/platform/bootdevice/by-name/boot"
ANDROID_BOARD_FLASH_BLOCK_SIZE = "131072"
