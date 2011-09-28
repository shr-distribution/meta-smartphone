require linux.inc

SECTION = "kernel"
DESCRIPTION = "Linux kernel for the Samsung Crespo device based on the offical \
source from Samsung"
KERNEL_IMAGETYPE = "uImage"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-crespo/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "f28873908974cb257c0e9699e8e052ba56fd6e7a"

KV = "2.6.35"
PR = "r1"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
