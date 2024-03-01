require recipes-kernel/linux/linux-yocto.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "hammerhead"

DESCRIPTION = "Kernel close to upstream with device specific patches intented to be mainlined.\
 Maintained by the PostmarketOS team."
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

#ANDROID_BOOTIMG_CMDLINE = "msm.vram=200m cma=300m g_mass_storage.removable=y LUNEOS_NO_OUTPUT_REDIRECT g_ffs.idVendor=0x18d1 g_ffs.idProduct=0xd001"
ANDROID_BOOTIMG_CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT user_debug=31 maxcpus=2 msm_watchdog_v2.enable=1 msm.allow_vram_carveout=1 msm.vram=300m cma=500m pty.legacy_count=8"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02900000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x02700000"

inherit kernel_android

LINUX_VERSION ?= "6.1"
LINUX_VERSION_EXTENSION = "-luneos"
LINUX_KMETA_BRANCH = "yocto-${LINUX_VERSION}"
KMETA = "kernel-meta"

SRCREV_machine = "da71f988f97df0023776f45738d3e93a481d721c"
SRCREV_meta = "142fcf0b8fcf1643d80e39e2c2f9c4a3bb528fcc"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=hammerhead/${LINUX_VERSION}/mainline;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
"

S = "${WORKDIR}/git"

PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
