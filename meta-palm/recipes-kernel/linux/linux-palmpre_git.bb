require linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
SRCREV = "bbf1c6f7d6e0bdff5ca99566466e646cd9e03433"
KV = "2.6.24"
PR="r0"
PV = "${KV}+gitr${SRCPV}"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=palmpre/devtmpfs \
  file://defconfig \
"
S = "${WORKDIR}/git/"
