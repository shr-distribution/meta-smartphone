require recipes-kernel/linux/linux.inc

SECTION = "kernel"
DESCRIPTION = "Modified kernel for the Palm Pre based on the original source from Palm Inc."
KERNEL_IMAGETYPE = "uImage"
COMPATIBLE_MACHINE = "(palmpre|palmpre2)"
SRCREV = "442f7e619d20d6b607c82bc42436788ffb82d66f"
KV = "2.6.24"
PE = "2"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=palmpre/2.6.24/master \
  file://defconfig \
"
S = "${WORKDIR}/git"
