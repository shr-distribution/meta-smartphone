require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mindphone"

DESCRIPTION = "Linux kernel for the GreenTouch (Mindphone) device based on the official \
source from GreenTouch"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom user_debug=23 msm_rtb.filter=0x3b7 ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=dnim/4.14.186;protocol=https"
S = "${WORKDIR}/git"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/k39tv1_64_bsp_defconfig_luneos ${WORKDIR}/defconfig
}

SRCREV = "39e40e1cf59b1d2aa05f9a45f8db25e0044b236c"

KV = "4.14.186"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
