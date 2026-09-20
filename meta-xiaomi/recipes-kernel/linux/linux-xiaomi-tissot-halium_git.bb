require recipes-kernel/linux/linux.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs from a
# kernel this old. See the include for why each one is there.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^tissot-halium$"

DESCRIPTION = "Linux kernel for the Xiaomi A1 (tissot) device based on the offical \
source from Xiaomi"

DEPENDS += "openssl-native"

# No lpm_levels.sleep_disabled=1 here. That flag is bring-up debris inherited
# from the Halium reference cmdline: it makes the Qualcomm lpm-levels driver
# refuse every low-power mode, so all eight cores idle in shallow WFI and the
# SoC can never enter platform suspend. Measured on sargo (same driver, same
# tree family): writing sleep_disabled=N at runtime engaged the deep C-states
# within seconds and the device stayed stable. tissot boots the same 4.9 msm
# tree, so drop it from the command line rather than undoing it after boot.
ANDROID_BOOTIMG_CMDLINE = "androidboot.console=ttyHSL0 androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci earlycon=msm_hsl_uart,0x78af000 firmware_class.path=/vendor/firmware_mnt/image androidboot.usbconfigfs=true androidboot.selinux=permissive androidboot.keymaster=1 androidboot.usbconfigfs=true --"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=tissot/4.9/halium-9.0;protocol=https"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/tissot_defconfig ${WORKDIR}/defconfig
}

SRCREV = "b3b9435a43a64fa1c3b0c748fe31e1641e92acea"

LINUX_VERSION = "4.9.188"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;" and assigns to it.
# C23 made constexpr a keyword, and this host GCC (15.x on Ubuntu 26.04) defaults
# to -std=gnu23, so building the kernel host tools fails:
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#   scripts/unifdef.c:880:27: error: expected identifier or '(' before '=' token
#
# Upstream renamed the variable in 6.7; this kernel predates that. Only the host
# tools are affected - the kernel itself is built with OE's cross toolchain -
# and kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host
# C standard here is enough.
BUILD_CFLAGS:append = " -std=gnu17"
