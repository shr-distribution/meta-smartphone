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

LINUX_VERSION ?= "6.9.1"
LINUX_VERSION_EXTENSION = "-luneos"
LINUX_KMETA_BRANCH = "yocto-6.6"
KMETA = "kernel-meta"

SRCREV_machine = "26f1d324c6edafd9c353f8cea734443f96073ef5"
SRCREV_meta = "4146b93ecdc92b420f16e5564806cd0fc9b09d13"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=msm8974-mainline/qcom-msm8974-6.9.y/main;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
"

S = "${WORKDIR}/git"

PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
