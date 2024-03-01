require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^rosy$"

DESCRIPTION = "Linux kernel for the Xiaomi Rosy (Redmi 5, Snapdragon) device based on the offical \
source from Xiaomi"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 lpm_levels.sleep_disabled=1 androidboot.bootdevice=7824900.sdhci earlycon=msm_hsl_uart,0x78af000 firmware_class.path=/vendor/firmware_mnt/image"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=rosy/3.18/halium-9.0;name=kernel;protocol=https \
    git://github.com/baunilla/android_qcom_opensource_wlan_prima.git;branch=wlan-driver.lnx.1.0.r30-rel;name=prima;destsuffix=git/drivers/staging/prima;protocol=https \
    file://lineageos_rosy_defconfig_7.1 \
"

S = "${WORKDIR}/git"

do_configure:prepend() {
    cp -v -f ${WORKDIR}/lineageos_rosy_defconfig_7.1 ${WORKDIR}/defconfig
}

SRCREV_kernel = "4dcf15ddaa13dd98e34aebd64371f94bdea41050"
SRCREV_prima = "404995ddd576b4bad2ca7274f151fa2ed6243d5e"
SRCREV_FORMAT = "kernel-prima"

LINUX_VERSION = "3.18.31"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append () {
    rm -rf ${D}/usr/src/usr
}
