require linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
SRCREV = "0b1014d99fc4992ece1605285e728c44d801a7a7"
KV = "2.6.24"
PR="r0"
PV = "${KV}+gitr${SRCPV}"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=palmpre/devtmpfs \
  file://defconfig \
"
S = "${WORKDIR}/git/"
