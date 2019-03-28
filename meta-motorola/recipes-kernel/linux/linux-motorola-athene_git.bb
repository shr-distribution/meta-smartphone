require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "athene"

DESCRIPTION = "Linux kernel for the Motorola G4 device based on the official \
source from Motorola"

ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.console=ttyHSL0 androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci lpm_levels.sleep_disabled=1 earlyprintk"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

SRC_URI = " \
  git://github.com/herrie82/android_kernel_motorola_msm8952.git;branch=cm-14.1 \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/athene_defconfig ${WORKDIR}/defconfig
}

SRCREV = "f8406b96363849c38204def56dfe7f10bc42c567"

KV = "3.10.107"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr

}
