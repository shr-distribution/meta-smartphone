require linux.inc

PR = "r0"
PE = "1"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-1"
DEFAULT_PREFERENCE_om-gta02 = "1"
DEFAULT_PREFERENCE_om-gta01 = "1"

SRC_URI = "${KERNELORG_MIRROR}/linux/kernel/v2.6/linux-${PV}.tar.bz2;name=kernel \
           ${KERNELORG_MIRROR}/linux/kernel/v2.6/longterm/v${PV}/patch-${PV}.10.bz2;apply=yes;name=stablepatch \
           file://defconfig "

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
SRC_URI[stablepatch.md5sum] = "56f59ee6f5f322d168bda0b360eb9f2b"
SRC_URI[stablepatch.sha256sum] = "1a7ba6692a233934fc131349ccdf19c272306d3a0b3db01046c76c4e7996d567"
