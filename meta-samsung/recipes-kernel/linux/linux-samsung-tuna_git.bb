require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tuna"

DESCRIPTION = "Linux kernel for the Samsung Tuna device based on the offical \
source from Samsung"

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=tuna/3.0/cm-12.1 \
  file://defconfig \
"

S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${B}/.config
      kernel_conf_variable $1 $2 ${B}/.config
  }

  # fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup USB_MUSB_OMAP2PLUS y
  kernel_conf_variable_fixup USB_G_ANDROID y
}

SRCREV = "049d90ea417234dc89ca4f68f5a5ab27de10ac37"

PE = "3"
KV = "3.0.101"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
