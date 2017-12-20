require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "onyx"

DESCRIPTION = "Linux kernel for the OnePlus Onyx (OnePlus X) device based on the official \
source from OnePlus"

CMDLINE = "androidboot.hardware=qcom user_debug=23 msm_rtb.filter=0x3b7 ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1"
KERNEL_RAM_BASE = "0x00008000" 
RAMDISK_RAM_BASE = "0x01000000"
SECOND_RAM_BASE = "0x00f00000" 
TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

SRC_URI = " \
  git://github.com/Halium/android_kernel_oneplus_onyx.git;branch=cm-14.1 \
"
S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm/configs/lineageos_onyx_defconfig ${WORKDIR}/defconfig
}

SRCREV = "b1a69fe454bc6d7f7f84ff52369c9ca56d6be3b7"

KV = "3.4.0"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

#Below was specifically for mido, might not be needed for onyx
#do_install_append () {
#    rm -rf ${D}/usr/src/usr
#
#}
