require linux.inc

SECTION = "kernel"
DESCRIPTION = "Linux kernel for the Samsung Crespo device based on the offical \
source from Samsung"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=samsung-crespo/master \
  file://defconfig \
"

S = "${WORKDIR}/git/"

SRCREV = "0ec6640edb480a8d6f6e43ab1bc39e2bf62de61d"

KV = "2.6.35"
PR = "r9"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
# disable cgroups (needed by systemd) for now as kernel fails to compile with it enabled
KERNEL_ENABLE_CGROUPS = "0"
