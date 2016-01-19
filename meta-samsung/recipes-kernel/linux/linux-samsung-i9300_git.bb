require recipes-kernel/linux/linux.inc

SECTION = "kernel"
DESCRIPTION = "Linux kernel for the Samsung Galaxy SIII device from CyanogenMod"

COMPATIBLE_MACHINE = "i9300"

KERNEL_RAM_BASE  = "0x40008000"
RAMDISK_RAM_BASE = "0x41000000"
EXTRA_ABOOTIMG_ARGS = '-c "pagesize=2048"'
inherit kernel_android

SRC_URI = " \
  git://github.com/CyanogenMod/android_kernel_samsung_smdk4412.git;protocol=git;branch=cm-13.0 \
  file://battery-quiet.patch \
  file://watchdog-quiet.patch \
  file://0001-kernel-add-support-for-gcc-5.patch \
"

S = "${WORKDIR}/git"

SRCREV = "764df269b7e4e2784ab4acb841e503b364552641"

KV = "3.0.101"
PV = "${KV}+gitr${SRCPV}"
inherit machine_kernel_pr


do_configure_prepend() {
  cp ${S}/arch/arm/configs/cyanogenmod_i9300_defconfig ${WORKDIR}/defconfig

  clear_var() {
    sed -i -e 's/^.*CONFIG_${1}.*//' ${WORKDIR}/defconfig
  }

  set_var() {
    clear_var $1
    echo "CONFIG_${1}=${2}" >> ${WORKDIR}/defconfig
  }

  unset_var() {
    clear_var $1
    echo "# CONFIG_${1} is not set" >> ${WORKDIR}/defconfig
  }

  unset_var VMWARE_MVP
  set_var TTY_PRINTK y
  set_var AUTOFS4_FS m
}
