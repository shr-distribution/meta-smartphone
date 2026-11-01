require recipes-kernel/linux/linux.inc
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs from a
# kernel this old. See the include for why each one is there.
require recipes-kernel/linux/waydroid-kernel.inc
# vince_defconfig sets MODULE_SIG_FORCE, so the out-of-tree modules have to be
# signed with this build's key.
require recipes-kernel/linux/kernel-module-signing.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^vince-halium$"

DESCRIPTION = "Linux kernel for the Xiaomi Redmi 5 Plus (vince), the 4.9 tree of the \
msm8953 LineageOS maintainers"

# LineageOS's own android_kernel_xiaomi_msm8953 has no vince_defconfig and no vince
# device tree on any branch, although android_device_xiaomi_vince names both. This
# repository, from the same maintainers, is the tree that has them.
#
# The command line is LineageOS's BOARD_KERNEL_CMDLINE (msm8953-common) with what
# tissot adds for LuneOS. No lpm_levels.sleep_disabled=1, for the reason given in
# the tissot recipe.
ANDROID_BOOTIMG_CMDLINE = "androidboot.console=ttyHSL0 androidboot.hardware=qcom msm_rtb.filter=0x237 ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci earlycon=msm_hsl_uart,0x78af000 firmware_class.path=/vendor/firmware_mnt/image androidboot.usbconfigfs=true androidboot.selinux=permissive androidboot.keymaster=1"

# BOARD_KERNEL_BASE, BOARD_MKBOOTIMG_ARGS and BOARD_KERNEL_PAGESIZE of msm8953-common.
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI = "git://github.com/xiaomi-msm8953-devs/temp_android_kernel_xiaomi_vince.git;branch=lineage-18.1;protocol=https \
           file://0001-hybris-always-boot-to-initramfs.patch \
           file://0002-scripts-use-pkg-config-to-locate-libcrypto.patch \
           file://0003-kbuild-Use-ls-1-instead-of-stat-1-to-obtain-file-siz.patch \
           file://0004-kbuild-simplify-rules-of-data-compression-with-size.patch \
           file://0005-techpack-don-t-inject-current-timestamp-and-fix-buil.patch \
           file://0006-techpack-Fix-incorrect-if-statements-detected-with-g.patch \
           file://0007-arm64-Remove-redundant-mov-from-LL-SC-cmpxchg.patch \
           file://0008-lib-build-OID-registry-don-t-embed-the-build-path-in.patch \
           file://0009-scripts-conmakehash-don-t-embed-the-build-path-in-ou.patch \
           file://0010-arm64-don-t-hook-vdso-prepare-into-external-module-b.patch \
           file://luneos.cfg \
"

SRCREV = "0fd7a835ce76582f456c017621fed4ee49be4552"

LINUX_VERSION = "4.9.251"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native openssl-native"

do_configure:prepend() {
    cat ${S}/arch/arm64/configs/vince_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

do_configure:append() {
    kernel_conf_variable_fixup() {
        sed -i "/CONFIG_$1[ =]/d" ${B}/.config
        kernel_conf_variable $1 $2 ${B}/.config
    }

    # USB gadget functions that oldconfig turns from y to m on these 4.9 msm trees,
    # which breaks the configfs gadget the initramfs and adb rely on. As on sargo
    # and fajita.
    kernel_conf_variable_fixup USB_LIBCOMPOSITE y
    kernel_conf_variable_fixup USB_F_ACM y
    kernel_conf_variable_fixup USB_U_SERIAL y
    kernel_conf_variable_fixup USB_U_ETHER y
    kernel_conf_variable_fixup USB_F_SERIAL y
    kernel_conf_variable_fixup USB_F_RNDIS y
    kernel_conf_variable_fixup USB_F_MASS_STORAGE y
    kernel_conf_variable_fixup USB_F_FS y
    kernel_conf_variable_fixup USB_F_MIDI y
    kernel_conf_variable_fixup USB_F_HID y
    kernel_conf_variable_fixup USB_F_MTP y
    kernel_conf_variable_fixup USB_F_PTP y
    kernel_conf_variable_fixup USB_F_AUDIO_SRC y
    kernel_conf_variable_fixup USB_F_ACC y
    kernel_conf_variable_fixup USB_CONFIGFS y

    oe_runmake olddefconfig
}

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;", which C23 made a keyword,
# and this host GCC defaults to -std=gnu23. See the tissot recipe.
BUILD_CFLAGS:append = " -std=gnu17"
