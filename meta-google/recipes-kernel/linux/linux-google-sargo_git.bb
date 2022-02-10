require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^sargo$"

DESCRIPTION = "Linux kernel for Google Pixel 3a device based on the \
sources from Droidian and LineageOS"

ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM0,115200n8 androidboot.console=ttyMSM0 printk.devkmsg=on msm_rtb.filter=0x237 ehci-hcd.park=3 service_locator.enable=1 firmware_class.path=/vendor/firmware datapart=/dev/mmcblk0p72 cgroup.memory=nokmem lpm_levels.sleep_disabled=1"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

SRC_URI = " \
    git://github.com/erikinkinen/linux-android-google-sargo.git;branch=lune \
    "
SRCREV = "20f655295a5fe56188ebef997671bc16a004a084"

S = "${WORKDIR}/git"

DEPENDS += "dtc-native python3-dtschema-wrapper-native"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineageos_bonito_defconfig ${WORKDIR}/defconfig
}


do_configure:append() {
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

KV = "4.9.124"
PV = "${KV}+git978c025"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append () {
    rm -rf ${D}/usr/src/usr
}
