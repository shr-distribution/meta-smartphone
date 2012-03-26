require recipes-kernel/linux/linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
COMPATIBLE_MACHINE = "(palmpre|palmpre2)"
SRCREV = "dc2ff991a816e51847cc5edd0bbbf19d1600ad49"
KV = "2.6.24"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config

SRC_URI = " \
  git://git.freesmartphone.org/linux-2.6.git;protocol=git;branch=palmpre/devtmpfs \
  file://defconfig \
"
S = "${WORKDIR}/git/"
