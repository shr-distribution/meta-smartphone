require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^surya$"

DESCRIPTION = "Linux kernel for the Xiaomi POCO X3 NFC (surya, SM7150 / \
SDMMAGPIE), the LineageOS 22.2 4.14 tree the /e/OS build on this device runs"

# VERIFIED against the shipping /e/OS boot.img
# (e-4.2-a15-20260816663279-community-surya), not just read out of the
# BoardConfig: header v2, 4096 byte pages, kernel 0x00008000, ramdisk
# 0x01000000, tags 0x00000100, dtb 0x01f00000, second 0x00000000 (no second
# image), os_version 15.0.0 / 2026-08. BOARD_KERNEL_BASE is 0x00000000 with the
# standard 0x8000 kernel offset, which is why the kernel address looks
# unusually low; sargo and athena use the same set.
#
# The cmdline is that image's, verbatim, plus androidboot.selinux=permissive.
# It matches the lineage-22.2 BoardConfig exactly, which is expected: /e/OS
# builds from that device tree.
ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom service_locator.enable=1 lpm_levels.sleep_disabled=1 loop.max_part=7 androidboot.init_fatal_reboot_target=recovery androidboot.selinux=permissive"
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x01f00000"
# 15.0.0, security patch 2026-08 - the stock value, read back out of the /e/OS
# image's header (raw field 0x1E0001A8) rather than encoded from a guess.
# Matching stock keeps any bootloader rollback check on this field happy; same
# reasoning as mindphone.
ANDROID_BOOTIMG_OS_VERSION = "0x1E0001A8"

# The header-v2 dtb section. surya sets BOARD_KERNEL_SEPARATED_DTBO, so the
# device-specific overlays live in dtbo.img (which stays stock) and this
# section carries only the SoC base tree. The stock section is a single bare
# FDT of 345,849 bytes, root model "Qualcomm Technologies, Inc. SDMMAGPIE SoC",
# qcom,msm-id <0x16d 0x0> - i.e. sdmmagpie.dtb, which this tree builds as the
# base of sdmmagpie-idp-overlay.dtbo (CONFIG_BUILD_ARM64_DT_OVERLAY=y).
# Left to the kernel's own build rather than shipping the stock blob: it is the
# same source revision the phone runs. Compare the built blob's size and root
# properties against the stock one before flashing; if they ever diverge, drop
# the stock dtb in next to this recipe and point ANDROID_BOOTIMG_DTB at it, the
# way mindphone does.
KERNEL_DEVICETREE = "qcom/sdmmagpie.dtb"

# boot is 134,217,728 B (128 MB) per BOARD_BOOTIMAGE_PARTITION_SIZE and the
# /e/OS image is padded to exactly that; stock content is a 17.8 MB kernel plus
# a 1.7 MB ramdisk, so there is no size pressure here at all.

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/LineageOS/android_kernel_xiaomi_surya.git;branch=lineage-22.2;protocol=https \
           file://0001-techpack-qcacld-drop-Werror-from-the-vendor-Kbuilds.patch \
           file://0002-init-keep-the-build-flags-out-of-linux_banner.patch \
           file://luneos.cfg \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
# lineage-22.2 @ "surya: Switch to ir-spi driver". Chosen to match the vendor:
# the /e/OS A15 build reports 4.14.356-openela-rc1 and this is the tree and
# branch it is built from, so the rebuilt kernel meets the same /vendor blobs
# the stock one does.
SRCREV = "21aca2e848f6697311ad801176e4c4aa4ddc52c7"

LINUX_VERSION = "4.14"
KV = "4.14.356"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native openssl-native"

# surya_defconfig is the config the LineageOS device tree selects
# (TARGET_KERNEL_CONFIG in device/xiaomi/surya/BoardConfig.mk). luneos.cfg is
# the LuneOS/Halium delta on top of it; later lines win over earlier ones when
# kconfig reads the concatenation.
#
# Verified with mer_verify_kernel_config: the config extracted from the
# shipping /e/OS kernel via IKCONFIG scores 8 errors / 54 warnings, and the
# defconfig+fragment concatenation resolved through kconfig scores 1 error
# (CONFIG_DUMMY, deliberate - see the fragment) / 29 warnings, all of them
# optional items.
do_configure:prepend() {
    cat ${S}/arch/arm64/configs/surya_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# Three files kbuild generates into ${B} record the absolute path of the tool
# or input they were made from, in a banner comment:
#
#   drivers/tty/vt/consolemap_deftbl.c    "conmakehash <abs>/drivers/tty/vt/cp437.uni"
#   drivers/video/logo/logo_linux_clut224.c  "generated from <abs>/...logo_linux_clut224.ppm"
#   lib/oid_registry_data.c               "Automatically generated by <abs>/lib/build_OID_registry"
#
# linux.inc ships the build tree as the -src package, so those paths go out with
# it and wrynose fails do_package_qa with "contains reference to TMPDIR
# [buildpaths]". Rewrite the banners to name the files the way an in-tree build
# would; they are comments, and the generated data below them is unchanged.
# Same treatment as athena and mindphone.
do_compile:append() {
    for f in drivers/tty/vt/consolemap_deftbl.c \
             drivers/video/logo/logo_linux_clut224.c \
             lib/oid_registry_data.c; do
        if [ -f ${B}/$f ]; then
            sed -i -e "s|${STAGING_KERNEL_DIR}/||g" -e "s|${B}/||g" ${B}/$f
        fi
    done
}

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;" and assigns to it. C23
# made constexpr a keyword and the host GCC here defaults to -std=gnu23, so
# building the kernel host tools (unifdef is pulled in by headers_install)
# fails:
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#
# Upstream renamed the variable in 6.7; this kernel predates that. Only the
# host tools are affected - the kernel itself is built with OE's cross
# toolchain - and kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so
# pinning the host C standard here is enough. Same fix as athena and tissot.
BUILD_CFLAGS:append = " -std=gnu17"
