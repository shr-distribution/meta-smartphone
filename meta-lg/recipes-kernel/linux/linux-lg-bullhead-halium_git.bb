require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^bullhead-halium$"

# The arm64 Makefile of 3.10 links libgcc.a (LIBGCC := $(CC) -print-libgcc-file-name), which a
# kernel recipe's sysroot does not have: "ld.bfd: cannot find libgcc.a". As linux-huawei-angler.
DEPENDS:append:aarch64 = " libgcc"
KERNEL_CC:append:aarch64 = " ${TOOLCHAIN_OPTIONS}"
KERNEL_LD:append:aarch64 = " ${TOOLCHAIN_OPTIONS}"

DESCRIPTION = "Linux kernel for the LG Bullhead (Nexus 5X) device, from the LineageOS 21 \
tree of github.com/nexus5x-dev"

# The command line of the LineageOS 21 boot.img (BOARD_KERNEL_CMDLINE in the device
# tree), without lpm_levels.sleep_disabled=1: that keeps every core out of the deep
# idle states, see linux-xiaomi-tissot-halium_git.bb. The systemd cgroup flags come
# from halium-kernel.inc through CONFIG_CMDLINE_EXTEND, which this kernel honours on
# the device tree path too (drivers/of/fdt.c, unlike the 3.4 kernels of hammerhead
# and mako).
#
# boot_cpus=0-3 maxcpus=4 nr_cpus=4 instead of LineageOS' boot_cpus=0-5: only the four
# Cortex-A53 cores, as the official TWRP for this phone boots. nr_cpus=4 (honoured since
# "arm64: smp: honour nr_cpus=") also keeps the A57s out of later hotplug, which the
# vendor's init.bullhead.power.sh does for cpu4 at boot. The first test phone (bootloader
# BHZ11h) panicked 0.3 s into boot with "failed to lock a57_pll1 PLL" (clock-pll.c) when the
# kernel brought up the A57 cluster, and hung in "Reboot failed -- System halted"; TWRP ran
# on the same phone. A57 PLL failures are what the Nexus 5X's big-core hardware fault looks
# like; whether that phone has it, or its old firmware is the cause, is not known yet.
ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.hardware=bullhead boot_cpus=0-3 maxcpus=4 nr_cpus=4 msm_poweroff.download_mode=0 loop.max_part=7 androidboot.boot_devices=soc.0/f9824900.sdhci androidboot.selinux=permissive"

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
# github.com/nexus5x-dev/kernel_lge_bullhead (3.10.108) at 995f521bafa3 with these LuneOS
# commits on top:
#
#  - arm64 proc.S: a section flag spelling the OE GNU as rejects
#  - qcacld-2.0: drop -Werror; GCC 15 diagnoses ~20 files the Android toolchain did not
#  - halium_bullhead_defconfig: lineageos_bullhead_defconfig plus SysV IPC, the IPC/UTS/PID
#    namespaces, devtmpfs, fhandle, autofs, device cgroup, devpts instances, vndbinder,
#    no paranoid network (the commit message has the reasons)
#  - MemAvailable in /proc/meminfo (the tenderloin commit): memorymanager refuses every app
#    launch without it
#  - ipc_router: group net_raw may bind without paranoid networking, as on the Galaxy A3
#    (2015): pm-service runs as system:net_raw and rmt_storage drops its capabilities, and
#    the router refused both
#  - halium_bullhead_defconfig: BT_HCIVHCI for bluebinder
#  - renameat2 wired into both syscall tables: the tree had the 3.15 backport, but its slots
#    returned ENOSYS, and Android 16's bionic renames through renameat2 with no fallback
#  - execveat() through /proc/self/fd: the LuneOS arm64 glibc is built for kernels from 4.9
#    on and has no fexecve() fallback without it (lxc-attach re-executes itself that way)
#  - arm64 smp: nr_cpus= limits the possible CPUs (see the command line above)
#  - lpm-levels: skip the A57 cluster when nr_cpus= leaves its CPUs out (it crashed in
#    lpm_probe on the error path)
#
# The tree already has what the Android 16 GSI needs from a kernel that the 3.4 kernels
# had to be patched for: getrandom, memfd_create, seccomp filters, ambient
# capabilities (PR_CAP_AMBIENT), PR_SET_VMA, the five loop driver fixes, and a NULL-safe
# msm_cpp firmware load. Not booted.
SRC_URI = "git://github.com/shr-distribution/linux.git;branch=bullhead/3.10/lineage-21.0;protocol=https"
SRCREV = "9cdee0c151b3d647868528a3459f66e134759035"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/halium_bullhead_defconfig ${WORKDIR}/defconfig
}

# The initramfs debug shell (init.sh stops in its adbd shell when /proc/cmdline has
# enable_adb). Built into CONFIG_CMDLINE rather than added to the boot image's command
# line, as on sargo and goyavewifi, so it does not depend on what the bootloader passes
# on. For a `fastboot boot` test image:
#   CONF=$(mktemp --suffix=.conf); echo 'LUNEOS_ENABLE_ADB = "1"' > $CONF
#   MACHINE=bullhead-halium bitbake -R $CONF linux-lg-bullhead-halium
LUNEOS_ENABLE_ADB ??= "0"
do_configure:append() {
    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
    fi
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
    # headers_install also leaves the Android staging uapi headers (ion.h, msm_ion.h) under
    # /usr/src/usr, which no package ships; as sargo, fajita and athena.
    rm -rf ${D}/usr/src/usr
}

# The host tools of a 3.10 tree predate C23, which the host GCC 15 defaults to; the test
# build outside bitbake used HOSTCFLAGS="-O2 -std=gnu17", as linux-xiaomi-tissot-halium does.
BUILD_CFLAGS:append = " -std=gnu17"
