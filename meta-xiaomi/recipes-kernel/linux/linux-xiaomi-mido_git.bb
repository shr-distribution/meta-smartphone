require recipes-kernel/linux/linux.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mido"

DESCRIPTION = "Linux kernel for the Xiaomi Mido (Redmi Note 4, Snapdragon) device based on the offical \
source from Xiaomi"

SRC_URI = " \
  git://github.com/herrie82/android_kernel_xiaomi_msm8953.git;branch=pgz-14.1-eb8 \
"
S = "${WORKDIR}/git"

CMDLINE = "${ANDROID_BOOTIMG_CMDLINE}"

do_configure_prepend() {
    cp -v -f ${S}/arch/arm64/configs/mido_defconfig ${WORKDIR}/defconfig
}

SRCREV = "f974d591e9553d614fe4cc56b190f9aa598c8197"

KV = "3.18.85"
PV = "${KV}+gitr${SRCPV}"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install_append () {
    rm -rf ${D}/usr/src/usr

}
