
DESCRIPTION = "Kernel close to upstream with device specific patches intented to be mainlined.\
 Maintained by the PostmarketOS team."
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
SECTION = "kernel"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

LINUX_VERSION ?= "6.9.1"
LINUX_VERSION_EXTENSION = "-luneos"
LINUX_KMETA_BRANCH = "yocto-6.6"
KMETA = "kernel-meta"

SRCREV_machine = "251860b8a6b89972507ed395942a5c9e0762cec5"
SRCREV_meta = "4146b93ecdc92b420f16e5564806cd0fc9b09d13"

SRC_URI = " \
    git://github.com/msm8953-mainline/linux.git;branch=${LINUX_VERSION}/main;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
"

KBUILD_DEFCONFIG = ""

S = "${WORKDIR}/git"

PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
