
DESCRIPTION = "Kernel close to upstream with device specific patches intented to be mainlined.\
 Maintained by the PostmarketOS team."
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mido"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

ANDROID_BOOTIMG_CMDLINE = "LUNEOS_NO_OUTPUT_REDIRECT earlycon console=ttyMSM0,115200"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

LINUX_VERSION ?= "6.5.2"
LINUX_VERSION_EXTENSION = "-luneos"
LINUX_KMETA_BRANCH = "yocto-6.5"
KMETA = "kernel-meta"

SRCREV_machine = "634db669f343239b63af0b522f94481166ff9685"
SRCREV_meta = "9c7dcb61723601208997d38455d9e6928851e5ff"

SRC_URI = " \
    git://github.com/msm8953-mainline/linux.git;branch=${LINUX_VERSION}/main;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
    file://defconfig \
"

KBUILD_DEFCONFIG = ""


S = "${WORKDIR}/git"

KV = "${LINUX_VERSION}"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
