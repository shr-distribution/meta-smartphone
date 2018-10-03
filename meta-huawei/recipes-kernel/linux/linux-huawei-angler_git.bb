require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "angler"

DEPENDS_append_aarch64 = " libgcc"
KERNEL_CC_append_aarch64 = " ${TOOLCHAIN_OPTIONS}"
KERNEL_LD_append_aarch64 = " ${TOOLCHAIN_OPTIONS}"

DESCRIPTION = "Linux kernel for the Google Nexus 6P (Huawei) device based on the offical \
source from Google"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=angler androidboot.console=ttyHSL0 msm_rtb.filter=0x37 ehci-hcd.park=3 lpm_levels.sleep_disabled=1 boot_cpus=0-3 no_console_suspend"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000" 
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000" 
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

SRC_URI = " \
  git://github.com/herrie82/android_kernel_huawei_angler.git;branch=cm-14.1 \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineageos_angler_defconfig ${WORKDIR}/defconfig
}

SRCREV = "3680219f6a6d5cf5079131de878f7ae3f3a28981"

KV = "3.10.73"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
