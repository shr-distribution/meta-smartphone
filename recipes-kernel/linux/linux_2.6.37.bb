require linux.inc

PR = "r5"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-1"
DEFAULT_PREFERENCE_om-gta02 = "1"
DEFAULT_PREFERENCE_om-gta01 = "1"

SRC_URI = "${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/linux-${PV}.tar.bz2;name=kernel \
           ${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/patch-${PV}.6.bz2;apply=yes;name=stablepatch \
           file://defconfig "

SRC_URI_append_om-gta02 = " \
           file://openmoko.patch \
           file://shr.patch \
           "
SRC_URI_append_om-gta01 = " \
           file://openmoko.patch \
           file://shr.patch \
           "

SRC_URI[kernel.md5sum] = "c8ee37b4fdccdb651e0603d35350b434"
SRC_URI[kernel.sha256sum] = "edbf091805414739cf57a3bbfeba9e87f5e74f97e38f04d12060e9e0c71e383a"
SRC_URI[stablepatch.md5sum] = "7bfe7642816c4e506eeb62b73f66c6f0"
SRC_URI[stablepatch.sha256sum] = "33ca5cd06c715672969c459c21f441d7d6c74cba3304f981a40216e8094337bb"
