require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mido"

DESCRIPTION = "Linux kernel for the Xiaomi Mido (Redmi Note 4, Snapdragon) device based on the offical \
source from Xiaomi"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 lpm_levels.sleep_disabled=1 androidboot.bootdevice=7824900.sdhci earlycon=msm_hsl_uart,0x78af000"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=mido/3.18/halium-7.1"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/mido_defconfig ${WORKDIR}/defconfig
}

SRCREV = "e1ca249e49d0f3371d94dd9dbe28a58e3eae3c6d"

KV = "3.18.85"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr

}
