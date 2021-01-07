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
    git://github.com/Tofee/android_kernel_xiaomi_rosy.git;branch=halium-9.0;name=kernel \
    git://github.com/baunilla/android_qcom_opensource_wlan_prima.git;branch=wlan-driver.lnx.1.0.r30-rel;name=prima;destsuffix=git/drivers/staging/prima \
    file://lineageos_rosy_defconfig_7.1 \
"
SRCREV_kernel = "9cfaad198ec42a475c420e339450ab0c7e540bc5"
SRCREV_prima = "404995ddd576b4bad2ca7274f151fa2ed6243d5e"

#SRC_URI = "git://github.com/Tofee/android_kernel_xiaomi_rosy.git;branch=halium-9.0"
#SRCREV = "9cfaad198ec42a475c420e339450ab0c7e540bc5"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${WORKDIR}/lineageos_rosy_defconfig_7.1 ${WORKDIR}/defconfig
#    cp -v -f ${S}/arch/arm64/configs/rosy-baunil_defconfig ${WORKDIR}/defconfig
}


KV = "3.18.31"
PV = "${KV}+gitr9cfaad"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
