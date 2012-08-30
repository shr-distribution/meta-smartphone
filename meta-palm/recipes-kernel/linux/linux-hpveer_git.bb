require recipes-kernel/linux/linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the HP Veer based on the original source from HP"
KERNEL_IMAGETYPE = "uImage"

COMPATIBLE_MACHINE = "hpveer"

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=hpveer/2.6.29/master \
  file://defconfig \
"
S = "${WORKDIR}/git/"

PE = "2"
SRCREV = "29c1c6bb0a8feb6e6124dc0df2a22542ef45ef0d"
KV = "2.6.29"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
