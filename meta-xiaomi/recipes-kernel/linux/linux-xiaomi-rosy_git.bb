require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^rosy$"

DESCRIPTION = "Linux kernel for the Xiaomi Rosy (Redmi 5, Snapdragon) device based on the offical \
source from Xiaomi"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 lpm_levels.sleep_disabled=1 androidboot.bootdevice=7824900.sdhci earlycon=msm_hsl_uart,0x78af000"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

SRC_URI = " \
  git://github.com/Tofee/android_kernel_xiaomi_msm8953.git;branch=los14.1 \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineageos_rosy_defconfig ${WORKDIR}/defconfig
}

SRCREV = "77531a217781482be659cc4c703330489c0910bd"

KV = "3.18.31"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
