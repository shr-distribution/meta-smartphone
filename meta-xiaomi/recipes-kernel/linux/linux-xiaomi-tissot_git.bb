require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^tissot$"

DESCRIPTION = "Linux kernel for the Xiaomi A1 (tissot) device based on the offical \
source from Xiaomi"

DEPENDS += "openssl-native"

SRC_URI = " \
  git://github.com/Tofee/tissot.git;branch=cm-14.1 \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/tissot_defconfig ${WORKDIR}/defconfig
}

SRCREV = "e8ef22278d9ab768dcd20bb3c8f64cd991832d7f"

KV = "3.18.31"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr
}
