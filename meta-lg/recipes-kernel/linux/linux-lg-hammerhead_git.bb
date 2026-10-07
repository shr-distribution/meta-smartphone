require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^hammerhead$"

DESCRIPTION = "Linux kernel for the LG Hammerhead (Nexus 5) device. Plain \
mainline from kernel.org: the Nexus 5 device tree (qcom-msm8974-lge-nexus5-hammerhead) \
and the drivers for most of its hardware are upstream."

# The console is blsp1_uart1 (serial0 in the DT). No msm.vram/msm.allow_vram_carveout:
# those parameters are gone from the mainline msm driver, as are the downstream
# user_debug, msm_watchdog_v2 and maxcpus=2 options this used to carry.
#
# No "earlycon": with it this kernel resets the phone before the initramfs runs,
# a reboot loop every ~10 s (the same image without it enumerates its USB gadget
# 7 s after boot). Do not add it back without a way to check it on the device.
ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM0,115200,n8 LUNEOS_NO_OUTPUT_REDIRECT pty.legacy_count=8"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02900000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x02700000"

inherit kernel_android

LINUX_VERSION = "7.3.0"
LINUX_VERSION_EXTENSION = "-luneos"
# Pair the kernel-cache with the kernel. There is no yocto-7.3 branch yet.
LINUX_KMETA_BRANCH = "yocto-7.2"
KMETA = "kernel-meta"

# v7.3-rc5, pinned by its commit (a tag is not on a branch, hence nobranch=1).
SRCREV_machine = "72d3fcf802c45d00b300f25b848a93c3a2bd7c7e"
SRCREV_meta = "f26871a964ee3984a78f5e8fa1001cf43e551a20"

SRC_URI = " \
    git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git;protocol=https;nobranch=1;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
    file://0001-drm-msm-scan-out-of-contiguous-buffers-when-the-display-has-no-IOMMU.patch \
"

# do_kernel_configcheck runs symbol_why.py, which parses the tree with the
# kconfiglib bundled in kern-tools-native. That copy cannot parse the
# "depends on <sym> if <cond>" form, which the kernel's own scripts/kconfig
# accepts. Same workaround linux-lg-mako and linux-mainline-8916.bbclass use.
KMETA_AUDIT = ""

# This recipe ships a complete defconfig rather than assembling a config from
# kernel-cache fragments, so do not let a machine's KBUILD_DEFCONFIG override it.
KBUILD_DEFCONFIG = ""

PV = "${LINUX_VERSION}+git"

# do_kernel_version_sanity_check wants PV to match the Makefile, and a release
# candidate reports "7.3-rc5", which PV cannot carry without a '-' in the
# package version (and 7.3-rc5 would then sort wrongly against 7.3.0). Same
# skip linux-mainline-8916.bbclass and the megi and raspberrypi kernels use.
# Drop it, and the rc pin above, once this moves to a 7.3 release.
KERNEL_VERSION_SANITY_SKIP = "1"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}

do_compile:append() {
    # arch/arm/tools/gen-mach-types stamps an absolute path under TMPDIR into the
    # banner comment of mach-types.h, which ships in the -src package and trips
    # the [buildpaths] QA check. Rewrite the banner; the defines are unchanged.
    if [ -f ${B}/arch/arm/include/generated/asm/mach-types.h ]; then
        sed -i 's|generated from .*/arch/arm/tools/mach-types!|generated from arch/arm/tools/mach-types!|' \
            ${B}/arch/arm/include/generated/asm/mach-types.h
    fi
}
