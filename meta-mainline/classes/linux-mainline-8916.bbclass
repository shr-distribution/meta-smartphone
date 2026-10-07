
DESCRIPTION = "Kernel close to upstream with device specific patches intended to be mainlined.\
 Maintained by the msm8916-mainline team (MSM8909/MSM8916/MSM8939)."
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
SECTION = "kernel"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

LINUX_VERSION ?= "7.3-rc2"
LINUX_VERSION_EXTENSION = "-luneos"
# Pair the kernel-cache with the kernel rather than inheriting the 8953 class's
# yocto-6.6, which is four releases behind this tree. linux-megi.inc already
# runs yocto-7.2 against a 7.2 kernel here.
LINUX_KMETA_BRANCH = "yocto-7.2"
KMETA = "kernel-meta"

# msm8916-mainline/${LINUX_VERSION}/main in shr-distribution/linux: the
# msm8916-mainline team's wip/msm8916/7.3-rc2 (717e5e25225035d13c09b376aab5f23a5d7abe61)
# with LuneOS' device patches committed on top, the same arrangement as
# linux-mainline-8953. The patches live there as commits, not as .patch files
# in this layer. The upstream wip branches are rebased onto each new -rc rather
# than extended, so a bump means rebasing these commits onto the new wip branch
# and pushing it as a new msm8916-mainline/<version>/main.
SRCREV_machine = "f9e0bf03c2fce153637d236a8d91564d74dd7fc2"
SRCREV_meta = "31a9aee38a2827fac9db03afde5bc9fe88b49957"

SRC_URI = " \
    git://github.com/shr-distribution/linux.git;branch=msm8916-mainline/${LINUX_VERSION}/main;protocol=https;name=machine \
    git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=${LINUX_KMETA_BRANCH};destsuffix=${KMETA} \
"

# do_kernel_configcheck runs symbol_why.py, which parses the tree with the
# kconfiglib bundled in kern-tools-native. That copy cannot parse the
# "depends on <sym> if <cond>" form:
#
#     drivers/usb/cdns3/Kconfig:5: error: couldn't parse
#     'depends on USB if !USB_GADGET': extra tokens at end of line
#
# The kernel's own scripts/kconfig accepts it, only kconfiglib does not, so the
# audit cannot run at all on this tree and takes the build down with it. Same
# workaround linux-megi.inc applies for the same line on 7.2; drop it once
# kern-tools-native carries a kconfiglib that understands the syntax.
KMETA_AUDIT = ""

# This recipe ships a complete defconfig rather than assembling a config from
# kernel-cache fragments, so do not let a machine's KBUILD_DEFCONFIG override it.
KBUILD_DEFCONFIG = ""


# PV may not carry a '-', so the upstream "7.3-rc2" is spelled "7.3+rc2" here.
# That no longer matches the regex do_kernel_version_sanity_check builds out of
# the Makefile ("^7\.3(\.0)?-rc2"), hence the skip - the version is correct, the
# check just cannot express an -rc whose PV had to be rewritten.
PV = "7.3+rc2+git"
KERNEL_VERSION_SANITY_SKIP = "1"

# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr
