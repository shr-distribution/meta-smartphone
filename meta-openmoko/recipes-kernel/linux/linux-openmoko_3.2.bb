require recipes-kernel/linux/linux.inc
require linux-openmoko.inc

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_om-gta02 = "1"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

COMPATIBLE_MACHINE = "(om-gta02)"

SRC_URI += "${KERNELORG_MIRROR}/linux/kernel/v3.x/linux-${PV}.tar.bz2;name=kernel \
           ${KERNELORG_MIRROR}/linux/kernel/v3.x/patch-${PV}.52.bz2;apply=yes;name=stablepatch \
           file://defconfig \
           "

SRC_URI_append_om-gta02 = " \
  file://shr.patch \
"

SRC_URI[kernel.md5sum] = "7ceb61f87c097fc17509844b71268935"
SRC_URI[kernel.sha256sum] = "c881fc2b53cf0da7ca4538aa44623a7de043a41f76fd5d0f51a31f6ed699d463"
SRC_URI[stablepatch.md5sum] = "12ff4fd955b5e4f5fb9b864c007cc619"
SRC_URI[stablepatch.sha256sum] = "b6771424d856d56e47c878efbdf30efaae659d4b61be5f5df8fe2bda880289db"
