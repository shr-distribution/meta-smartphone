require recipes-kernel/linux/linux.inc
require linux-openmoko.inc

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-2"

COMPATIBLE_MACHINE = "(om-gta02)"

SRC_URI += "${KERNELORG_MIRROR}/linux/kernel/v3.x/linux-${PV}.tar.bz2;name=kernel \
           ${KERNELORG_MIRROR}/linux/kernel/v3.x/patch-${PV}.15.bz2;apply=yes;name=stablepatch \
           file://defconfig \
           "

SRC_URI_append_om-gta02 = " \
  file://shr.patch \
"

SRC_URI[kernel.md5sum] = "7ceb61f87c097fc17509844b71268935"
SRC_URI[kernel.sha256sum] = "c881fc2b53cf0da7ca4538aa44623a7de043a41f76fd5d0f51a31f6ed699d463"
SRC_URI[stablepatch.md5sum] = "2232a36274115f053ce4adfb7cc00588"
SRC_URI[stablepatch.sha256sum] = "08a42c51f7dc33641f7110cf6fb82e1b7c86c7d68ae7ea26eb3075b877239d29"
