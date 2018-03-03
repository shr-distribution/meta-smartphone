require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "angler"

DEPENDS_append_aarch64 = " libgcc"
KERNEL_CC_append_aarch64 = " ${TOOLCHAIN_OPTIONS}"
KERNEL_LD_append_aarch64 = " ${TOOLCHAIN_OPTIONS}"

DESCRIPTION = "Linux kernel for the Google Nexus 6P (Huawei) device based on the offical \
source from Google"

SRC_URI = " \
  git://github.com/herrie82/android_kernel_huawei_angler.git;branch=cm-14.1 \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineageos_angler_defconfig ${WORKDIR}/defconfig
}

SRCREV = "3680219f6a6d5cf5079131de878f7ae3f3a28981"

KV = "3.10.73"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
