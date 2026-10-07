require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mako"

DESCRIPTION = "Linux kernel for the LG Mako (Nexus 4) device based on the \
apq8064-mainline tree (codeberg.org/LogicalErzor/linux)"

# LUNEOS_BOOTLOG keeps a boot log on the userdata partition (/data/luneos-boot.log in recovery)
# No earlycon: the bootloader only brings up the jack UART when it sees the debug
# cable, and an early console write to the clock-gated GSBI4 block hangs the bus.
# postmarketOS boots mako with the same console= and fw_devlink=permissive.
# panic=5 reboots five seconds after a kernel panic, so a panic is visible as a restart loop.
ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM2,115200,n8 fw_devlink=permissive panic=5 LUNEOS_NO_OUTPUT_REDIRECT LUNEOS_BOOTLOG"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80208000"
# The kernel decompresses to 0x80208000 (ARCH_QCOM_RESERVE_SMEM keeps it clear of
# the SMEM at the start of RAM), runs to about 0x81a0dac4 including BSS, and the
# zImage then relocates itself to just after that, ending near 0x82350000. A
# ramdisk anywhere below that gets overwritten, so load it well clear.
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x84000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x81100000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80200100"

inherit kernel_android

LINUX_VERSION = "7.1.0"
LINUX_VERSION_EXTENSION = "-luneos"
# Pair the kernel-cache with the kernel rather than inheriting an older branch.
LINUX_KMETA_BRANCH = "yocto-7.2"
KMETA = "kernel-meta"

SRCREV_machine = "15fbd13fe2475cf77fca0006b618bb5c929025bd"
SRCREV_meta = "31a9aee38a2827fac9db03afde5bc9fe88b49957"

SRC_URI = " \
    git://codeberg.org/LogicalErzor/linux.git;branch=apq8064;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
    file://0001-drm-panel-add-LG-LH467WX1-SD01-DSI-panel-driver-Nexu.patch \
    file://0002-leds-lm3530-add-device-tree-support.patch \
    file://0003-ARM-dts-qcom-apq8064-lg-nexus4-mako-enable-display-a.patch \
    file://0004-ARM-dts-qcom-apq8064-lg-nexus4-mako-enable-USB-gadge.patch \
    file://0005-ARM-dts-qcom-apq8064-lg-nexus4-mako-keep-the-earjack-debug-supply-on.patch \
"

# do_kernel_configcheck runs symbol_why.py, which parses the tree with the
# kconfiglib bundled in kern-tools-native. That copy cannot parse the
# "depends on <sym> if <cond>" form:
#
#     drivers/usb/cdns3/Kconfig:5: error: couldn't parse
#     'depends on USB if !USB_GADGET': extra tokens at end of line
#
# The kernel's own scripts/kconfig accepts it, only kconfiglib does not.
# Same workaround linux-mainline-8916.bbclass applies for the same issue.
KMETA_AUDIT = ""

# This recipe ships a complete defconfig rather than assembling a config from
# kernel-cache fragments, so do not let a machine's KBUILD_DEFCONFIG override it.
KBUILD_DEFCONFIG = ""

PV = "${LINUX_VERSION}+git"
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
