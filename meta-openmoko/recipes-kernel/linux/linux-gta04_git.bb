require recipes-kernel/linux/linux.inc

KERNEL_RELEASE = "3.4.9"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
PE = "4"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRCREV_kernel = "37b0bacc00e81811dc113265a9ef826fe9aab788"
SRCREV_init = "47dd9fd631f1908f3fcbabaf8fd48ba1503c2ea2"
SRCREV_FORMAT = "kernel_init"

SRC_URI = "\
  git://github.com/shr-distribution/linux.git;protocol=git;branch=om-gta04/3.4/master;name=kernel \
  git://github.com/radekp/gta04-init.git;protocol=git;branch=master;name=init;destsuffix=git/gta04-init \
  file://defconfig \
"
S = "${WORKDIR}/git"

do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${S}/.config
      kernel_conf_variable $1 $2 ${S}/.config
  }

  # fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup USB_MUSB_OMAP2PLUS y
  kernel_conf_variable_fixup USB_OMAP y
  kernel_conf_variable_fixup USB_GADGET_MUSB_HDRC y
}

CMDLINE_om-gta04 = "console=ttyO2,115200n8 mpurate=800 vram=12M omapfb.rotate_type=0 omapdss.def_disp=lcd root=/dev/mmcblk0p2 rw rootfstype=ext4,ext3,btrfs,ubifs,jffs2 rootwait twl4030_charger.allow_usb=1 twl4030_charger.charge_backup=1 musb_hdrc.preserve_vbus=1 log_buf_len=8M ignore_loglevel no_console_suspend verbose"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "om-gta04"

DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_om-gta04 = "1"

# after last kernel.bbclass changes whole KERNEL_SRC_PATH is pulled to kernel-dev including files from gta04-init, causing:
# ERROR: QA Issue: non debug package contains .debug directory: kernel-dev path
# /work/om_gta04-oe-linux-gnueabi/linux-gta04/linux-gta04-3_3.4.5+gitr3+315314d3ad42ce468f6eb7b58fabc5ddf4137267_1+47dd9fd631f1908f3fcbabaf8fd48ba1503c2ea2-r7/packages-split/kernel-dev/usr/src/kernel/gta04-init/bin/.debug/ubiattach
PACKAGES =. "kernel-gta04-init-dbg "
FILES_kernel-gta04-init-dbg = "${KERNEL_SRC_PATH}/gta04-init/bin/.debug ${KERNEL_SRC_PATH}/gta04-init/.debug"
