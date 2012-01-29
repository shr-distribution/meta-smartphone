require linux.inc

SECTION = "kernel"
DESCRIPTION = "Linux kernel for the Samsung Crespo device based on the offical \
source from Samsung"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-crespo/master-next \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "ef8c2b192ba8b2db3256cef3cdbf1d850d674604"

KV = "3.0.8"
PR = "r1"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
