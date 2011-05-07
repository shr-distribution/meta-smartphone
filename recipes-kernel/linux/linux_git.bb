require linux.inc

KERNEL_RELEASE = "2.6.39-rc6"
OLD_KERNEL_RELEASE = "2.6.38"
PV = "${OLD_KERNEL_RELEASE}+${KERNEL_RELEASE}+gitr${SRCPV}"

SRCREV = "c2bf807eb347325988b1c7f9139e934ed9b1d795"

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
