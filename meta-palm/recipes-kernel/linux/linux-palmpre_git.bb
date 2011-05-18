require linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
SRCREV = "2c70e5959c6ff34bb10a557a048f5afc32039ca6"
KV = "2.6.24"
PR="r0"
PV = "${KV}+gitr${SRCPV}"

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=palmpre/master \
  file://defconfig \
"
S = "${WORKDIR}/git/"
