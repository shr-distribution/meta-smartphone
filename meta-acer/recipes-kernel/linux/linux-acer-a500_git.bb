require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "a500"

DESCRIPTION = "Linux kernel for the Acer IconiaTab A500 device based on the offical \
source from Acer, modified to boot on the SDCard"

# This is the kernel 3.1.10 for A500, based on nvidia-tegra 3.1.10-android. The support for the "picasso" board
# of the A500 tablet has been added, and some code has been ported from the original Acer kernel 2.6.39.4.
SRC_URI = " \
  git://github.com/Tofee/picasso-kernel.git;protocol=git;branch=master \
  file://defconfig \
"
SRCREV = "5ff650c7145a834cccf5110125ab06c252265eaa"

S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${B}/.config
      kernel_conf_variable $1 $2 ${B}/.config
  }

  # fixup some options which get changes from Y to M in oldconfig :/
  kernel_conf_variable_fixup MPU_SENSORS_MPU3050 y
  kernel_conf_variable_fixup MPU_SENSORS_KXTF9 y
  kernel_conf_variable_fixup USB_TEGRA y
  kernel_conf_variable_fixup USB_G_ANDROID y
}

PV = "3.1.10"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"

