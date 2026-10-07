require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^bullhead-halium$"

DESCRIPTION = "Linux kernel for the LG Bullhead (Nexus 5X) device, from the LineageOS 21 \
tree of github.com/nexus5x-dev"

# The command line of the LineageOS 21 boot.img (BOARD_KERNEL_CMDLINE in the device
# tree), without lpm_levels.sleep_disabled=1: that keeps every core out of the deep
# idle states, see linux-xiaomi-tissot-halium_git.bb. The systemd cgroup flags come
# from halium-kernel.inc through CONFIG_CMDLINE_EXTEND, which this kernel honours on
# the device tree path too (drivers/of/fdt.c, unlike the 3.4 kernels of hammerhead
# and mako).
ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=bullhead boot_cpus=0-5 msm_poweroff.download_mode=0 loop.max_part=7 androidboot.boot_devices=soc.0/f9824900.sdhci androidboot.selinux=permissive"

# The header of the LineageOS 21 boot.img (lineage-21.0-20240911-UNOFFICIAL-bullhead.zip):
# page size 4096, kernel at 0x8000, ramdisk at 0x2000000, tags at 0x1e00000, no
# second stage (0).
ANDROID_BOOTIMG_PAGESIZE = "4096"
# The header version 0 path of kernel_android.bbclass builds with abootimg, which does
# not read ANDROID_BOOTIMG_PAGESIZE (same as linux-blackberry-athena_git.bb).
ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS = "-c pagesize=4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x01e00000"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# github.com/shr-distribution/linux, branch bullhead/3.10/lineage-21.0: lineage-21.0 of
# github.com/nexus5x-dev/kernel_lge_bullhead (3.10.108) at 995f521bafa3 with three LuneOS
# commits on top:
#
#  - arm64 proc.S: a section flag spelling the OE GNU as rejects
#  - qcacld-2.0: drop -Werror; GCC 15 diagnoses ~20 files the Android toolchain did not
#  - halium_bullhead_defconfig: lineageos_bullhead_defconfig plus SysV IPC, the IPC/UTS/PID
#    namespaces, devtmpfs, fhandle, autofs, device cgroup, devpts instances, vndbinder,
#    no paranoid network (the commit message has the reasons)
#
# The tree already has what the Android 16 GSI needs from a kernel that the 3.4 kernels
# had to be patched for: renameat2, getrandom, memfd_create, seccomp filters, ambient
# capabilities (PR_CAP_AMBIENT), PR_SET_VMA, the five loop driver fixes, and a NULL-safe
# msm_cpp firmware load. Built with the OE aarch64 GCC 15.3 outside bitbake; not booted.
SRC_URI = "git://github.com/shr-distribution/linux.git;branch=bullhead/3.10/lineage-21.0;protocol=https"
SRCREV = "42cae6897577d63e3c84dec945105311d0e4af69"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/halium_bullhead_defconfig ${WORKDIR}/defconfig
}

LINUX_VERSION = "3.10.108"
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

# The host tools of a 3.10 tree predate C23, which the host GCC 15 defaults to; the test
# build outside bitbake used HOSTCFLAGS="-O2 -std=gnu17", as linux-xiaomi-tissot-halium does.
BUILD_CFLAGS:append = " -std=gnu17"
