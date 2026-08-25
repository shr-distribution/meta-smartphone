require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^s2$"

# DEPENDS += "libgcc"

DESCRIPTION = "Linux kernel for the LeEco Le2 device based on the offical \
source from LeEco"

ANDROID_BOOTIMG_CMDLINE = "console=ttyHSL0,115200,n8 androidboot.console=ttyHSL0 androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci lpm_levels.sleep_disabled=1 earlyprintk buildvariant=userdebug"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=leeco-s2/3.10/halium-7.1;protocol=https \
           file://0001-lib-build_OID_registry-don-t-embed-the-build-path-i.patch \
           file://0002-scripts-conmakehash-don-t-embed-the-build-path-in-o.patch \
           "

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineage_s2_defconfig ${WORKDIR}/defconfig
}

SRCREV = "5baf5356a021136340782582db601deff5f38149"

LINUX_VERSION = "3.10.84"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}
