require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "onyx"

DESCRIPTION = "Linux kernel for the OnePlus Onyx (OnePlus X) device based on the official \
source from OnePlus"

ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom user_debug=23 msm_rtb.filter=0x3b7 ehci-hcd.park=3 androidboot.bootdevice=msm_sdcc.1"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=onyx/3.4.0/cm-14.1-los;protocol=https"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm/configs/lineageos_onyx_defconfig ${WORKDIR}/defconfig
}

SRCREV = "6895e8cc24276b4f80783b73e4c256bb63273010"

LINUX_VERSION = "3.4.0"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}
