require linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the HP Veer based on the original source from HP"
KERNEL_IMAGETYPE = "uImage"

COMPATIBLE_MACHINE = "hpveer"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=hp-veer/master \
  file://defconfig \
"
S = "${WORKDIR}/git/"

SRCREV = "29c1c6bb0a8feb6e6124dc0df2a22542ef45ef0d"
KV = "2.6.29"
PR="r0"
PV = "${KV}+gitr${SRCPV}"

# Workaround default -Werror setting and some warnings in kernel compilation
TARGET_CC_KERNEL_ARCH += " -Wno-error=unused-but-set-variable -Wno-error=array-bounds"
