require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^yggdrasil$"

DESCRIPTION = "Linux kernel for the Volla Phone device based on the offical \
source from Volla"

ANDROID_BOOTIMG_CMDLINE = "bootopt=64S3,32N2,64N2"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x14f88000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00e88000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x13f88000"

inherit kernel_android

SRC_URI = " \
    git://github.com/herrie82/android_kernel_volla_mt6763.git;branch=halium-9.0-LuneOS;      \
    "
SRCREV = "eb5a7ed936d75f599388bd5640acde6e03e4c381"

S = "${WORKDIR}/git"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/k63v2_64_bsp_defconfig ${WORKDIR}/defconfig
}


do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${B}/.config
      kernel_conf_variable $1 $2 ${B}/.config
  }

# fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup USB_LIBCOMPOSITE y
  kernel_conf_variable_fixup USB_F_ACM y
  kernel_conf_variable_fixup USB_U_SERIAL y
  kernel_conf_variable_fixup USB_U_ETHER y
  kernel_conf_variable_fixup USB_F_SERIAL y
  kernel_conf_variable_fixup USB_F_RNDIS y
  kernel_conf_variable_fixup USB_F_MASS_STORAGE y
  kernel_conf_variable_fixup USB_F_FS y
  kernel_conf_variable_fixup USB_F_MIDI y
  kernel_conf_variable_fixup USB_F_HID y
  kernel_conf_variable_fixup USB_F_MTP y
  kernel_conf_variable_fixup USB_F_PTP y
  kernel_conf_variable_fixup USB_F_AUDIO_SRC y
  kernel_conf_variable_fixup USB_F_ACC y
  kernel_conf_variable_fixup USB_CONFIGFS y
  oe_runmake oldnoconfig
}

KV = "4.4.243"
PV = "${KV}+gitr9cfaad"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
