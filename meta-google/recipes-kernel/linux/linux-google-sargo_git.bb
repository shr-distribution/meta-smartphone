require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs. Already
# correct on sargo's stock defconfig, but the options here are forced rather
# than appended, so requiring it too is a harmless no-op that keeps every
# waydroid-capable device consistent instead of relying on each one's
# defconfig happening to already agree.
require recipes-kernel/linux/waydroid-kernel.inc

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

# Android 11's bootloader will not take the v0 header abootimg produces. Stock
# and TWRP both ship header version 2 with 4096 byte pages and the device tree
# in its own section; a v0 image is refused with "Error boot prepare" before
# the kernel is ever reached. Addresses below are stock's.
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x01f00000"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=sargo/${LINUX_VERSION}/lune;protocol=https \
           file://0001-arm64-dts-sdm670-label-the-pshold-restart-node-msm_p.patch \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRCREV = "0102a71cd2fd99360db60b8ae70c5e3c758b1008"

LINUX_VERSION = "4.9.124"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native python3-dtschema-wrapper-native openssl-native"

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

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}
