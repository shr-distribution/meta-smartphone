require recipes-kernel/linux/linux.inc

PE = "2"
KERNEL_RELEASE = "3.13"
PV = "${KERNEL_RELEASE}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRCREV = "c089e5f8ba2d44afd2db2c2042d43ca70205c536"

SRC_URI = "\
  git://github.com/shr-distribution/linux.git;protocol=git;branch=nokia900/3.13/master \
  file://defconfig \
"
S = "${WORKDIR}/git"

do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${S}/.config
      kernel_conf_variable $1 $2 ${S}/.config
  }

  # fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup TWL4030_USB y
  kernel_conf_variable_fixup USB_MUSB_OMAP2PLUS y
}

KERNEL_EXTRA_ARGS = "LOADADDR=${UBOOT_LOADADDRESS}"

CMDLINE_nokia900 = "root=/dev/mmcblk0p1 rootwait rw console=ttyO2,115200n8 console=tty0 omapfb.vram=0:2M,1:2M,2:2M mtdoops.mtddev=2"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "nokia900"
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_nokia900 = "1"
