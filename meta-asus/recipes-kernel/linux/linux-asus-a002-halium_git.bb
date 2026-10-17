require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^a002$"

# The arm64 Makefile of 3.18 links libgcc.a (LIBGCC := $(CC) -print-libgcc-file-name),
# which a kernel recipe's sysroot does not have: "ld.bfd: cannot find libgcc.a".
# As linux-lg-bullhead-halium and linux-huawei-angler.
DEPENDS:append:aarch64 = " libgcc"
KERNEL_CC:append:aarch64 = " ${TOOLCHAIN_OPTIONS}"
KERNEL_LD:append:aarch64 = " ${TOOLCHAIN_OPTIONS}"

DESCRIPTION = "Linux kernel for the Asus Zenfone AR (A002 / ZS571KL), from the \
stock Asus msm8996 3.18 tree with the Halium/Android-16 GSI backports"

# The command line of the stock A002 boot.img (BOARD_KERNEL_CMDLINE in
# device/asus/A002/BoardConfig.mk), plus firmware_class.path and
# androidboot.selinux=permissive that Halium/GSI needs. The systemd cgroup-v1
# flags are added by halium-kernel.inc through CONFIG_CMDLINE_EXTEND, which this
# 3.18 kernel honours on the device-tree path (drivers/of/fdt.c), so they are
# not repeated here.
ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=qcom user_debug=31 msm_rtb.filter=0x237 ehci-hcd.park=3 lpm_levels.sleep_disable firmware_class.path=/vendor/firmware_mnt/image androidboot.selinux=permissive"

# The header of the stock A002 boot.img (read from the factory QFIL image):
# page size 4096, base 0x80000000, kernel at 0x80008000, ramdisk at 0x81000000,
# tags at 0x80000100, no second stage. Header version 0 keeps the abootimg
# path, which sets these addresses from the *_RAM_BASE variables below
# (kernel_android.bbclass) but does not read ANDROID_BOOTIMG_PAGESIZE, so the
# page size is passed to abootimg explicitly (as linux-lg-bullhead-halium).
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS = "-c pagesize=4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# github.com/shr-distribution/linux, branch zs571kl/3.18/gsi: the stock Asus
# ZS571KL kernel (3.18.31, github.com/Herrie82/zs571kl_stock_kernel) with these
# LuneOS commits on top (what the Android 16 GSI needs from the kernel):
#
#  - staging: android: multi-context binder backported from msm-3.18
#    (lineage-15.1): binder,hwbinder,vndbinder instead of the single /dev/binder
#    the stock tree has
#  - fs: execveat(2) backport (upstream 51f39a1f0cea): the LuneOS arm64 glibc is
#    built for kernels from 4.9 on and has no fexecve() fallback without it
#    (lxc-attach re-executes itself that way)
#  - qcom: -I$(src) added to the driver Makefiles that ship local headers, for
#    the out-of-tree-style include path the OE build needs
#  - arm64: add halium_A002_defconfig for the Android 16 GSI (do_configure copies
#    it): msmcortex_defconfig plus the namespaces, SysV IPC, multi-binder devices,
#    cgroups the LXC container/systemd/GSI need, and CONFIG_PCBID_FLAG (the stock
#    tree's drivers/base/firmware_class.c reads the Asus g_ASUS_rfSKU global
#    unconditionally, so it does not build without it)
#  - arm64: proc.S uses "ax" section flags the GNU as accepts (the stock tree's
#    #alloc,#execinstr is junk on aarch64 under the OE binutils)
#
# renameat2, getrandom, memfd_create and seccomp are already present in 3.18.
#
# NOTE: SRCREV covers the defconfig + proc.S commits plus the scripts/dtc YYLOC
# fix (810faf41, upstream e33a814e772c) that lets the host dtc link under GCC's
# -fno-common default, so `make scripts` / make-mod-scripts and out-of-tree
# modules build. All are on zs571kl/3.18/gsi and pushed to shr-distribution.
SRC_URI = "git://github.com/shr-distribution/linux.git;branch=zs571kl/3.18/gsi;protocol=https"
SRCREV = "02ebde907782bfdd33bed7232d3c8986852c6f42"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/halium_A002_defconfig ${WORKDIR}/defconfig
}

LINUX_VERSION = "3.18.31"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    # headers_install also leaves the Android staging uapi headers under
    # /usr/src/usr, which no package ships; as bullhead, sargo, fajita.
    rm -rf ${D}/usr/src/usr
}

# Kbuild writes the absolute path of the tool or input into the banner of a few
# generated files, and linux.inc ships the build tree as the -src package, so
# wrynose rejects it with "contains reference to TMPDIR [buildpaths]". Rewrite
# the banners to name the files the way an in-tree build would; they are
# comments, and the generated data below them is unchanged. Same treatment as
# radon, athena and sm-t220.
do_compile:append() {
    for f in drivers/tty/vt/consolemap_deftbl.c \
             drivers/video/logo/logo_linux_clut224.c \
             lib/oid_registry_data.c; do
        if [ -f ${B}/$f ]; then
            sed -i -e "s|${STAGING_KERNEL_DIR}/||g" -e "s|${B}/||g" ${B}/$f
        fi
    done
}

# The initramfs debug shell, built into CONFIG_CMDLINE rather than added to the
# boot image's command line, as on bullhead and sargo. For a test image:
#   CONF=$(mktemp --suffix=.conf); echo 'LUNEOS_ENABLE_ADB = "1"' > $CONF
#   MACHINE=a002 bitbake -R $CONF linux-asus-a002-halium
LUNEOS_ENABLE_ADB ??= "0"
do_configure:append() {
    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
    fi
}

# The host tools of a 3.18 tree predate C23, which the host GCC 15 defaults to,
# so build them as gnu17 (as linux-xiaomi-tissot-halium and linux-lg-bullhead-
# halium). -fcommon too: this tree's scripts/dtc declares YYLTYPE yylloc as a
# tentative definition in both dtc-lexer.lex and dtc-parser.tab, which GCC 10+'s
# -fno-common default rejects ("multiple definition of 'yylloc'") at HOSTLD of
# scripts/dtc/dtc. Same fix as linux-samsung-goyavewifi.
BUILD_CFLAGS:append = " -std=gnu17 -fcommon"
