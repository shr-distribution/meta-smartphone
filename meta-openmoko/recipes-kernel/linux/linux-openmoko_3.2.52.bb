require recipes-kernel/linux/linux.inc
require linux-openmoko.inc

# Mark archs/machines that this kernel supports
DEFAULT_PREFERENCE = "-2"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

COMPATIBLE_MACHINE = "(om-gta02)"

SRCREV = "93d64d150293b27f7a102c51a15335f3fe510214"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;protocol=https;branch=${MACHINE}/${PV}/master \
    file://defconfig \
"

S = "${WORKDIR}/git"
