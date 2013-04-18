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
SRCREV = "5e4bbed972c4bb65d0d24a4bb6ae9fefde202820"

CONFIG_CMDLINE = "tegraid=20.1.3.0.0 mem=1024M@0M androidboot.serialno=0428000342bfe117 video=tegrafb console=tty0 debug_uartport=hsport usbcore.old_scheme_first=1 lp0_vec=8192@0x3d805000 tegra_fbmem=8197120@0x3d81c000 brand=acer target_product=a500_ww_gen1 tegraboot=sdmmc gpt gpt_sector=62320639 androidboot.carrier=wifi-only bootloader_ver=0.03.14-MUL root=/dev/mmcblk1p2 rootwait"

S = "${WORKDIR}/git/"

PV = "3.1.10"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"

KERNEL_RAM_BASE = "0x10000000"
inherit kernel_android


