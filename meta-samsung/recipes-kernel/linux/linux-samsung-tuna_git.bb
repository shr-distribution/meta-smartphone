require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tuna"

DESCRIPTION = "Linux kernel for the Samsung Tuna device based on the offical \
source from Samsung"

CMDLINE = "console=ttyFIQ0 androidboot.console=ttyFIQ0 mem=1G vmalloc=768M omap_wdt.timer_margin=30 no_console_suspend"
KERNEL_RAM_BASE = "0x80008000"
RAMDISK_RAM_BASE = "0x81000000"
SECOND_RAM_BASE = "0x80f00000"
TAGS_RAM_BASE = "0x80000100"

inherit kernel_android

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=tuna/3.0/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${S}/.config
      kernel_conf_variable $1 $2 ${S}/.config
  }

  # fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup USB_MUSB_OMAP2PLUS y
  kernel_conf_variable_fixup USB_G_ANDROID y
}

SRCREV = "307425decdd5e274dc6d926035f63c6cbfd3bea2"

PE = "3"
KV = "3.0.72"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
