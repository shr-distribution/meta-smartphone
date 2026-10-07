# Shared recipe body for the Unihertz MT6771 kernels.
#
# The three Titans run the same SoC and the same boot geometry, but NOT the
# same kernel tree: the Titan is on the Android-10 ALPS drop (4.14.141) and the
# Pocket/Slim are on the Android-11 one (4.14.186), in two different
# repositories. So each device gets a thin recipe that names its tree, its
# defconfig, its dtb and its os_version, and everything else lives here.
#
# Neither tree has GPL source from Unihertz - they refuse, and have since 2020.
# Both are Nikita Ukhrenkov's (TheKit / NotKit) work: a MediaTek ALPS BSP with
# the device-specific drivers disassembled out of the stock kernel binaries and
# rewritten. Treat him as the upstream.

require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Boot geometry. IDENTICAL on the Titan and the Titan Pocket - verified against
# both UBports deviceinfo files, which carry the same pagesize and the same five
# offsets. Only the dtb and the os_version differ, and those are per recipe.
#
# UBports states these as a base plus offsets; kernel_android.bbclass wants the
# absolute addresses, so they are added up here:
#
#   base    0x40000000
#   kernel  + 0x00080000 = 0x40080000
#   ramdisk + 0x15000000 = 0x55000000
#   second  + 0x00f00000 = 0x40f00000   (no second image; kept to match stock)
#   tags    + 0x14000000 = 0x54000000
#   dtb     + 0x14000000 = 0x54000000   (v2 header dtb section, same address)
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "2048"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x40080000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x55000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x40f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x54000000"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x54000000"

# AVB. The stock vbmeta does not hash boot, it *chains* it: `avbtool info_image`
# on the Titan Pocket's 2024-05-14 vbmeta.img shows chain descriptors for
# vbmeta_system (location 2), vbmeta_vendor (4) and boot (3), and the stock
# boot.img carries its own footer and RSA2048-signed vbmeta struct. What lets
# our unsigned boot image through is the kit's vbmeta-disabled.img (the stock
# vbmeta with flags 3); the footer matches stock's shape in case lk reads it
# itself, and is what UBports ship for Pocket and Slim
# (deviceinfo_bootimg_partition_size, the same 32 MiB).
ANDROID_BOOTIMG_AVB_PARTITION_SIZE = "33554432"

# bootopt is how MediaTek lk knows the SoC/kernel/userland word sizes - it
# parses and consumes the token itself. Keep it first and do not touch it.
#
# Deliberately NOT carried over from the UBports cmdline:
#   security=apparmor  LuneOS does not confine with AppArmor. Their whole
#                      LSM-as-a-loadable-module arrangement exists for that and
#                      is not our problem.
#   rodata=0           needed only to insmod modules into the *stock* kernel.
#                      We build our own.
#   init=/init         the initramfs is the init; the kernel does this already.
#
# HAZARD, unmeasured on this SoC: q25's Helio G99 lk silently drops the first
# 20 characters of the boot-image command line, and q25.conf pads with '?'
# accordingly. mindphone's MT6739 4.14-era lk does not, and its cmdline is this
# same shape, so no padding is applied here. If early arguments appear to be
# ignored, compare /proc/cmdline against this string before blaming anything
# else - and put anything that must survive into CONFIG_CMDLINE instead, which
# halium-kernel.inc already extends (CONFIG_CMDLINE_EXTEND) for the cgroup-v1
# pair systemd needs.
ANDROID_BOOTIMG_CMDLINE = "bootopt=64S3,32N2,64N2 buildvariant=user log_buf_len=4M printk.devkmsg=on"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native openssl-native"

# The device defconfig with the LuneOS/Halium delta merged on top; later lines
# win over earlier ones when kconfig reads the concatenation. Each recipe sets
# UNIHERTZ_DEFCONFIG to the name under arch/arm64/configs.
#
# Note we do NOT use the tree's own halium.config, even where one exists (the
# Titan branch has one). files/luneos.cfg is a superset of it minus the
# AppArmor block, so one fragment serves every machine in this layer and there
# is a single place to change.
UNIHERTZ_DEFCONFIG ?= ""

# Optional per-device fragments applied after luneos.cfg, for things that are
# true of one machine and not its siblings. Named files, unpacked from SRC_URI.
UNIHERTZ_EXTRA_CFG ?= ""

do_configure:prepend() {
    if [ ! -f ${S}/arch/arm64/configs/${UNIHERTZ_DEFCONFIG} ]; then
        bbfatal "UNIHERTZ_DEFCONFIG '${UNIHERTZ_DEFCONFIG}' not found in this kernel tree"
    fi
    cat ${S}/arch/arm64/configs/${UNIHERTZ_DEFCONFIG} ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
    for f in ${UNIHERTZ_EXTRA_CFG}; do
        if [ ! -f ${UNPACKDIR}/$f ]; then
            bbfatal "UNIHERTZ_EXTRA_CFG names '$f', which is not in SRC_URI"
        fi
        cat ${UNPACKDIR}/$f >> ${WORKDIR}/defconfig
    done
}

# Three files kbuild generates into ${B} record the absolute path of the tool
# or input they were made from, in a banner comment. linux.inc ships the build
# tree as the -src package, so those paths go out with it and wrynose fails
# do_package_qa with "contains reference to TMPDIR [buildpaths]". Rewrite the
# banners to name the files the way an in-tree build would; they are comments,
# and the generated data below them is unchanged. Same treatment as athena.
do_compile:append() {
    for f in drivers/tty/vt/consolemap_deftbl.c \
             drivers/video/logo/logo_linux_clut224.c \
             lib/oid_registry_data.c; do
        if [ -f ${B}/$f ]; then
            sed -i -e "s|${STAGING_KERNEL_DIR}/||g" -e "s|${B}/||g" ${B}/$f
        fi
    done
}

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind,
    # and linux.inc ships everything under ${exec_prefix}/src/linux* as
    # kernel-headers. Those files record absolute command lines, which wrynose
    # rejects as "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;" and assigns to it. C23
# made constexpr a keyword and the host GCC here defaults to -std=gnu23, so
# building the kernel host tools (unifdef is pulled in by headers_install)
# fails with "'constexpr' in empty declaration". Upstream renamed the variable
# in 6.7; these trees predate that. Only the host tools are affected, and
# kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host C
# standard is enough. Same fix as athena, tissot and mindphone.
BUILD_CFLAGS:append = " -std=gnu17"
