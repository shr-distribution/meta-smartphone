require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# GCC 15 / PIE build fixes for 3.4 trees.
require recipes-kernel/linux/halium-kernel-3.4.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^sm-t520$"

DESCRIPTION = "Linux kernel for the Samsung Galaxy Tab Pro 10.1 Wi-Fi (SM-T520, \
n2awifi, Exynos 5420), built from the exynos5420 community's LineageOS 18.1 branch"

#-----------------------------------------------------------------------------
# Source
#-----------------------------------------------------------------------------
# github.com/shr-distribution/linux, branch sm-t520/3.4/lineage-18.1: the exynos5420
# community's LineageOS 18.1 kernel (github.com/exynos5420/android_kernel_samsung_exynos5420,
# branch lineage-18.1, commit de0cdcf10dcc, 10 Sep 2023) with the changes this port needs
# applied as commits, each by its own author (the GCC 15, pid namespace and net namespace
# backports from the hammerhead Halium tree, and the Exynos driver fixes), so that the
# recipe carries no kernel patches. Samsung's 3.4.113 drop for the Exynos 5420 tablets
# (n1a, n2a, v1a, v2a, chagall, klimt) with the community's Android 11 work on top is
# the kernel the LineageOS 18.1 build of this tablet boots, so it already has binder with
# hwbinder/vndbinder, ashmem, seccomp, getrandom and memfd_create.
#
# The kernel is board-file based, not device-tree based: arch/arm/boot/dts has
# nothing for the Exynos 5420, and the stock boot.img has a zero-length dt
# section. The bootloader passes ATAGs.
#
# Pinned to the branch head as of 6 Oct 2026. The six commits above 40d2148ac330 import the Linux backports
# 4.2-rc7 Bluetooth stack (from the hammerhead Halium tree), each by its own author, because the 3.4 kernel's
# own stack speaks mgmt 1.0 and never answers StartDiscovery. 83bb8b8 makes the Wi-Fi driver drop, while
# suspended, multicast IP that an access point turned into unicast (an IGMP query every 20 s woke the tablet).
# fe70beff402f adds clock_gettime64 and clock_getres_time64 (see halium-kernel-3.4.inc for why).
SRCREV = "fe70beff402f349e54910ad0f962c6b20e48a98d"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=sm-t520/3.4/lineage-18.1;protocol=https \
           file://luneos.cfg \
           "

LINUX_VERSION = "3.4.113"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

#-----------------------------------------------------------------------------
# Boot image
#-----------------------------------------------------------------------------
# Read from the stock boot.img of T520XXUAOI2 (SAMFW factory image) and found
# unchanged in the LineageOS 17.1 build of this tablet: page size 2048, kernel
# 0x10008000, ramdisk 0x11000000, second 0x10f00000, tags 0x10000100 (that is
# BOARD_KERNEL_BASE 0x10000000 plus the usual offsets), header version 0, no dtb.
#
# The stock header carries an empty cmdline; the kernel's CONFIG_CMDLINE (CMDLINE
# below) is extended by the bootloader's, and halium-kernel.inc appends the
# systemd cgroup v1 flags to it.
ANDROID_BOOTIMG_CMDLINE = "buildvariant=userdebug"

# os_version and the header cmdline are the two header fields in which an image the
# bootloader runs differed from one it did not, found on the tablet on 4 Oct 2026. The
# bootloader ignores the cmdline's contents (only CONFIG_CMDLINE and its own string reach
# the kernel), but an image whose header had os_version 0 and an empty cmdline, which is
# what this class wrote by default, never got to /init: no log, no panic, a reset to the
# Galaxy Tab logo. Both values are copied from the LineageOS boot image that was on the
# tablet (os_version 0x0e041156 is 2021-06, its cmdline "buildvariant=userdebug").
# TODO: find out whether one of the two is enough.
ANDROID_BOOTIMG_OS_VERSION = "0x0e041156"

# linux.inc writes CONFIG_CMDLINE from this variable (plus " loglevel=3") over
# whatever the defconfig had, so the stock string has to be given here or it is
# lost: the built kernel had " loglevel=3 systemd.unified_cgroup_hierarchy=0 ..."
# and none of the rest. This is lineageos_n2awifi_defconfig's own value:
#   console=ttySAC2,115200n8       the debug UART (see SERIAL_CONSOLE)
#   vmalloc=512M                    the kernel's vmalloc area; the ION/Mali
#                                   memory on this 2 GB board lives in it
#   androidboot.console / .hardware the container's init reads these from
#                                   /proc/cmdline; hardware=universal5420 is what
#                                   the vendor's init.universal5420.rc is named for
#   androidboot.selinux=permissive  as the LineageOS build has it
CMDLINE = "console=ttySAC2,115200n8 vmalloc=512M androidboot.console=ttySAC2 androidboot.hardware=universal5420 androidboot.selinux=permissive"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x10008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x11000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x10f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x10000100"

inherit kernel_android

# The initramfs is xz-compressed to fit the 8 MiB BOOT partition; see
# initramfs-android-image.bbappend. Set HERE and not in sm-t520.conf:
# kernel_android.bbclass assigns INITRAMFS_NAME itself.
INITRAMFS_NAME = "initramfs-android-image-${MACHINE}.cpio.xz"

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

FILESEXTRAPATHS:prepend := "${THISDIR}/linux-samsung-sm-t520:"

# lineageos_n2awifi_defconfig is the config the LineageOS 18.1 build uses.
# luneos.cfg is the LuneOS/Halium delta on top: later lines win over earlier
# ones when kconfig reads the concatenation (it warns about each override).
do_configure:prepend() {
    cat ${S}/arch/arm/configs/lineageos_n2awifi_defconfig \
        ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# The backports' Bluetooth options are not visible to kconfig until BACKPORT_LINUX is set, so the oldconfig
# pass that sets it drops them. Put them back and run oldconfig again until they stay.
do_configure:append() {
    for pass in 1 2 3; do
        grep -E '^(# CONFIG_BT is not set|CONFIG_BACKPORT|CONFIG_CRYPTO_CMAC)' ${UNPACKDIR}/luneos.cfg >> ${B}/.config
        yes '' | oe_runmake_call -C ${S} O=${B} oldconfig > /dev/null
    done
    grep -q '^CONFIG_BACKPORT_BT_HCIUART_H4=y' ${B}/.config || bbfatal "the backports Bluetooth options did not stay in .config"
}

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}
