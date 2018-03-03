require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "grouper"

DESCRIPTION = "Linux kernel for the Asus Grouper device"

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=grouper/3.1/master \
  file://defconfig \
"

S = "${WORKDIR}/git"

SRCREV = "0bc115f0e3511c09becce80b044e9b6271955d28"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

KV = "3.1.10"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
