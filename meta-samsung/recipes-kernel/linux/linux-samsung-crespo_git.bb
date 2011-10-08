require linux.inc

SECTION = "kernel"
DESCRIPTION = "Linux kernel for the Samsung Crespo device based on the offical \
source from Samsung"
KERNEL_IMAGETYPE = "zImage"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-crespo/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "1598d1b0c0f2b94ee35223bc7fc341c16b68d496"

KV = "2.6.35"
PR = "r3"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
