require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# GCC 15 / PIE build fixes for 3.4 trees.
require recipes-kernel/linux/halium-kernel-3.4.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^flo-halium$"

DESCRIPTION = "Linux kernel for the Google/Asus Nexus 7 (2013) Wi-Fi (flo, razor, \
APQ8064), built from LineageOS' 18.1 kernel for it"

#-----------------------------------------------------------------------------
# Source
#-----------------------------------------------------------------------------
# github.com/shr-distribution/linux, branch flo/3.4/lineage-18.1: LineageOS' kernel for the
# Nexus 7 (2013) (github.com/LineageOS/android_kernel_google_msm, branch lineage-18.1, commit
# 0da5107137f9, 3 Jun 2026), the one its LineageOS 18.1 (flox) build boots, with what this port
# needs applied as commits, each by its own author, so that the recipe carries no kernel patches:
#
#   - renameat2 (flags 0), clock_gettime64/clock_getres_time64 and the GCC 7/8/15 build fixes,
#     as on sm-t520/3.4/lineage-18.1, the other Lineage 18.1 3.4 kernel;
#   - the pid namespace's setns() support (upstream "pidns: Add setns support" and what followed
#     it on mako/3.4/halium-9.0): this tree declared pidns_operations but never defined it, so
#     PID_NS, which the container needs, did not link; and per-namespace ifindex;
#   - Linux backports 4.2-rc7 built into the kernel for Bluetooth (management interface 1.10;
#     the 3.4 stack's 1.0 never answers bluetoothd's discovery), with the L2CAP shutdown fixes,
#     and a port of the in-tree hci_smd driver to it, for the WCNSS (Riva) controller, which
#     speaks HCI over shared memory;
#   - the cgroup fixes of the other 3.4 Halium kernels, a loop driver race fix, an ALSA NULL op
#     check, and the V4L2 core bits v4l2loopback 0.15.x needs (mako's version, which keeps the
#     MSM video driver's EOS buffer flag).
#   - two fixes of this tree's own backports: shmem_tmpfile() called the generic_acl_init() that
#     "fs: remove generic_acl" had removed (no build with TMPFS_POSIX_ACL, which meta-oe's linux.inc
#     forces on), and the backports compat idr_alloc() clashed with the V4L2 forward-port's.
#
# memfd_create is at 385 in this tree's syscall table, as on the SM-T520, so the renameat2 and
# time64 commits are that branch's variants. The tree already has binder with hwbinder and
# vndbinder, ashmem, seccomp, getrandom and memfd_create, which the Android 11 build needs.
# It is board-file based: no device tree, the bootloader passes ATAGs.
SRCREV = "da994bce9d283bfac75c0a002eee363f0c42b608"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=flo/3.4/lineage-18.1;protocol=https \
           file://luneos.cfg \
           "

LINUX_VERSION = "3.4.113"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

#-----------------------------------------------------------------------------
# Boot image
#-----------------------------------------------------------------------------
# Read from the boot.img of lineage-18.1-20260923-UNOFFICIAL-flox.zip: header version 0, page
# size 2048, kernel 0x80208000, ramdisk 0x82200000 (BOARD_MKBOOTIMG_ARGS --ramdisk_offset
# 0x02000000 on BOARD_KERNEL_BASE 0x80200000), tags 0x80200100, no second stage (its address is
# mkbootimg's default offset, as for mako). The command line is that image's, which is
# BOARD_KERNEL_CMDLINE of android_device_asus_flox: androidboot.hardware=flox is what the
# vendor's init.flox.rc is named for, and vmalloc=340M is what the GPU and ION carve-outs need,
# as on the Nexus 4. The Nexus bootloader passes the header's command line on.
ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=flox user_debug=31 msm_rtb.filter=0x3F ehci-hcd.park=3 vmalloc=340M"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80208000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x82200000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x81100000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80200100"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

FILESEXTRAPATHS:prepend := "${THISDIR}/linux-asus-flo-halium:"

# lineageos_flo_defconfig is the config the LineageOS 18.1 build uses.
# luneos.cfg is the LuneOS/Halium delta on top: later lines win over earlier
# ones when kconfig reads the concatenation (it warns about each override).
do_configure:prepend() {
    cat ${S}/arch/arm/configs/lineageos_flo_defconfig \
        ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# The backports' Bluetooth options are not visible to kconfig until BACKPORT_LINUX is set, so the oldconfig
# pass that sets it drops them. Put them back and run oldconfig again until they stay.
do_configure:append() {
    for pass in 1 2 3; do
        grep -E '^(# CONFIG_BT is not set|CONFIG_BACKPORT|CONFIG_CRYPTO_CMAC)' ${UNPACKDIR}/luneos.cfg >> ${B}/.config
        yes '' | oe_runmake_call -C ${S} O=${B} oldconfig > /dev/null
    done
    grep -q '^CONFIG_BACKPORT_BT_HCISMD=y' ${B}/.config || bbfatal "the backports Bluetooth options did not stay in .config"
}

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}
