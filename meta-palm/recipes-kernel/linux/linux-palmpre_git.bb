require linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
SRCREV = "4ed85bdd7b091e40800a95e99afa22f32250c107"
KV = "2.6.24"
PR="r0"
PV = "${KV}+gitr${SRCPV}"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=palmpre/devtmpfs \
  file://defconfig \
"
S = "${WORKDIR}/git/"
