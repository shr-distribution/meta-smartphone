require recipes-kernel/linux/linux-unihertz-mt6771.inc

DESCRIPTION = "Linux kernel for the Unihertz Titan (MT6771), from TheKit's \
reverse-engineered MediaTek ALPS 4.14 tree - Unihertz publish no source"

COMPATIBLE_MACHINE = "^titan$"

# A DIFFERENT tree from the Pocket/Slim one: the Titan sits on the Android-10
# ALPS drop at 4.14.141, in the UBports GitLab group rather than on GitHub, and
# it is the branch that is actually alive - September 2026 commits rewrote the
# imx219 and ov16880 camera drivers in C and merged the two storage variants.
# It is also the only one of the three whose source-built kernel is known to
# boot a glibc/systemd userland, because the Ubuntu Touch port ships it.
SRC_URI = "git://gitlab.com/ubports/porting/community-ports/android10/unihertz-titan/kernel-alps-mt6771.git;branch=halium-10.0;protocol=https \
           file://luneos.cfg \
           file://unihertz-titan.dtb \
"

# halium-10.0 @ "imgsensor: ov16880: rewrite the disassembled driver in C",
# 14 Sep 2026, the branch head.
SRCREV = "fc94895e6dccf5f8f2cc34abc0255e2a3e4edebe"

LINUX_VERSION = "4.14"
KV = "4.14.141"

# One defconfig for both the eMMC and the UFS Titan since Sep 2026: it is the
# union of the two storage stacks, and upstream verified it by booting the UFS
# build on an eMMC unit. Every UFS path is gated at runtime on
# get_boot_type() == BOOTDEV_UFS, which lk reports through atag,boot.
UNIHERTZ_DEFCONFIG = "titan_defconfig"

# Stock dt_table blob (magic 0xd7b7ab1e), 112,135 bytes, one entry, page_size
# 2048 - a different board from the Pocket/Slim one, so not shared.
ANDROID_BOOTIMG_DTB = "${UNPACKDIR}/unihertz-titan.dtb"

# 10.0.0, security patch 2025-08, as the UBports deviceinfo stamps it.
ANDROID_BOOTIMG_OS_VERSION = "0x14000198"
