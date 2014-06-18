require recipes-kernel/linux/linux.inc

DESCRIPTION = "Linux kernel for HP Touchpad"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "tenderloin"

SRC_URI = " \
  git://github.com/shr-distribution/linux.git;protocol=git;branch=tenderloin/3.0/master \
  file://defconfig \
"
S = "${WORKDIR}/git"

SRCREV = "c355b0503504376623f33b84ecab33cc78f2bdc1"

do_compile[depends] += "initramfs-android-image:do_rootfs"
DEPENDS += "abootimg-native initramfs-android-image"

KV = "3.0.101"
PV = "${KV}+gitr${SRCPV}"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
