require linux.inc

KERNEL_RELEASE = "2.6.39-rc5"
OLD_KERNEL_RELEASE = "2.6.38"
PV = "${OLD_KERNEL_RELEASE}+${KERNEL_RELEASE}+gitr${SRCPV}"

SRCREV = "fafc9929c668f8bae6dd1f109f33a86d2cb3c460"

SRC_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux-2.6.git;protocol=git;branch=master \
           file://defconfig"

#           file://shr.patch \
SRC_URI_append_om-gta02 = " \
           file://openmoko.patch \
           "
#           file://shr.patch \
SRC_URI_append_om-gta01 = " \
           file://openmoko.patch \
           "

S = "${WORKDIR}/git"

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-2"
DEFAULT_PREFERENCE_spitz = "1"
