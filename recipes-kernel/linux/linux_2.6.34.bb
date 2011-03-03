require linux.inc

PR = "r7"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-1"
DEFAULT_PREFERENCE_om-gta01 = "1"
DEFAULT_PREFERENCE_om-gta02 = "1"

SRC_URI = "${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/linux-${PV}.tar.bz2;name=kernel \
           ${KERNELORG_MIRROR}/pub/linux/kernel/v2.6/longterm/v2.6.34/patch-${PV}.8.bz2;apply=yes;name=stablepatch \
           file://ARM-Add-support-for-LZMA-compressed-kernel-images.patch;status=pending \
           file://defconfig"

SRC_URI_append_om-gta01 = " \
           file://openmoko.patch \
           file://shr.patch \
           "

SRC_URI_append_om-gta02 = " \
           file://openmoko.patch \
           file://shr.patch \
           "

SRC_URI[kernel.md5sum] = "10eebcb0178fb4540e2165bfd7efc7ad"
SRC_URI[kernel.sha256sum] = "fa395fec7de633df1cb85b6248b8f35af98380ed128a8bc465fb48bc4d252633"
SRC_URI[stablepatch.md5sum] = "de755877dbd32ed783067987c095c278"
SRC_URI[stablepatch.sha256sum] = "1fb5cb404ab2b2aa57e967a34c2882a9a9df40fa2e4de8c1463f22ac82d5c9bf"
