SUMMARY = "Kernel drivers for the PowerVR SGX chipset found in the omap4 SoCs"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://GPL-COPYING;md5=60422928ba677faaa13d6ab5f5baaa1e"

SRC_URI = "git://github.com/CyanogenMod/android_hardware_ti_omap4.git;branch=stable/cm-12.1-YOG7D;protocol=git \
    file://0001-Set-correct-Android-version-for-bitbake-build.patch;striplevel=2  \
"

PV = "0.24.9+gitr${SRCPV}"
SRCREV = "177ddb61bb5f02ce1ff5b37bdb891189ca3e1e53"

COMPATIBLE_MACHINE = "^maguro$"

S = "${WORKDIR}/git/pvr-source/"

inherit module

# on Android side:
# make -j8 -C hardware/ti/omap4/pvr-source/eurasiacon/build/linux2/omap4430_android 
# ARCH=arm KERNEL_CROSS_COMPILE=arm-eabi- CROSS_COMPILE=arm-eabi- 
# KERNELDIR=/media/LuneOS/Android/CM/out/target/product/maguro/obj/KERNEL_OBJ 
# TARGET_PRODUCT="blaze_tablet" BUILD=release TARGET_SGX=540

MAKE_TARGETS = "-C eurasiacon/build/linux2/omap4430_android TARGET_PRODUCT=\"blaze_tablet\" BUILD=release TARGET_SGX=540 KERNELDIR=${STAGING_KERNEL_DIR}"

module_do_install() {
    install -d ${D}/lib/modules/${KERNEL_VERSION}/extra
    install -v -m644 ${S}/eurasiacon/binary2_540_120_omap4430_android_release/target/kbuild/pvrsrvkm_sgx540_120.ko ${D}/lib/modules/${KERNEL_VERSION}/extra
}

KERNEL_MODULE_AUTOLOAD = "pvrsrvkm_sgx540_120"
