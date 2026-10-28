require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^pioneer$"

DESCRIPTION = "Linux kernel for the Sony Xperia XA2 (pioneer, SDM630), the 4.19 \
LineageOS tree for the Ganges/Nile platform"

# lineage-23.2 and lineage-24.0 point at the same commit, so this one kernel
# matches both vendors pioneer may run (Android 16 and Android 17).
SRC_URI = "git://github.com/LineageOS/android_kernel_sony_sdm660.git;branch=lineage-24.0;protocol=https \
           file://0001-fanotify-declare-old_memcg-in-fanotify_alloc_event.patch \
           file://luneos.cfg \
"
SRCREV = "ee6b64dc037d6003e3164d17b16e68cc6aa616ba"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

LINUX_VERSION = "4.19"
KV = "4.19.325"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

# 4.19's COPYING is the SPDX preamble, not the GPLv2 text linux.inc expects.
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

DEPENDS += "dtc-native openssl-native"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# Left empty: kernel.bbclass exports LOCALVERSION from it and linux.inc also
# writes CONFIG_LOCALVERSION, so any value lands twice (sunfish). The tree's own
# CONFIG_LOCALVERSION="-perf" does NOT survive: linux.inc deletes it and writes
# CONFIG_LOCALVERSION="${LOCALVERSION}" (empty) and LOCALVERSION_AUTO=n, so the
# release is 4.19.325-cip136-st20 (measured, uname -r), not the shipped
# 4.19.325-cip136-st20-perf-gee6b64dc037d. Nothing here loads modules by path.
KERNEL_LOCALVERSION = ""

#-----------------------------------------------------------------------------
# Boot image.
#
# VERIFIED field-for-field against the shipping image, NOT the board file - the
# two disagree, as they did on sunfish, and the shipped image wins. boot_b of an
# H4113 on lineage-24.0-20261015-NIGHTLY (sha256 662a5e07...0810):
#
#   header_version 0, page_size 4096, no AVB footer
#   kernel_addr  0x80008000   19,927,413 B Image.gz-dtb (one appended dtb)
#   ramdisk_addr 0x81000000   board file: BOARD_RAMDISK_OFFSET 0x02000000
#   tags_addr    0x80000100   board file: BOARD_KERNEL_TAGS_OFFSET 0x01E00000
#   second_addr  0x00000000   size 0
#   cmdline      nile-common's BOARD_KERNEL_CMDLINE, byte for byte
#   os_version   17.0.0, patch level 2026-10
#
# That kernel ends at 0x81309175, 3 MB PAST its own ramdisk address, and boots
# every day: Sony's xboot relocates the ramdisk (as surya's ABL does,
# boot-images.md § The kernel/ramdisk window). So the bound that is proven on
# this bootloader is not the nominal window but the shipped kernel's size, and
# do_deploy below asserts that instead.
#-----------------------------------------------------------------------------
ANDROID_BOOTIMG_HEADER_VERSION = "0"
ANDROID_BOOTIMG_PAGESIZE = "4096"
# The v0 path drives abootimg, which ignores ANDROID_BOOTIMG_PAGESIZE and
# defaults to 2048 byte pages (athena).
ANDROID_BOOTIMG_EXTRA_ABOOTIMG_ARGS = "-c pagesize=4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x81000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x80000100"
# The shipped image's os_version word: 17.0.0 / 2026-10. It is NOT cosmetic
# here. xboot evidently hands the header's version and patch level to TZ: the
# keymaster TA then accepts only a configure() carrying the same pair, and the
# GSI's phh on-post-data.sh (fixSPL) feeds keymaster exactly what it reads back
# out of the boot_<slot> PARTITION (getSPL). With 0 here (the class default) it
# read "0.0.0" / "2000-00-01", and keymaster-3-0 aborted every 5 s on
#
#   android.hardware.keymaster@3.0-impl: Invalid patch month 0
#   KeyMasterHalDevice: resp->status: -38        (KM_ERROR_INVALID_ARGUMENT)
#
# Measured 28 Oct 2026: with this word RAM-booted (fastboot boot) and keymaster
# handed 17.0.0 / 2026-10-01, configure succeeded and IKeymasterDevice/default
# registered; with the header at 0, neither 0/0 nor 17.0.0/2026-10 did. A
# RAM boot alone cannot fix it, since getSPL reads the flashed partition.
# Keep it at the shipped value, never lower.
ANDROID_BOOTIMG_OS_VERSION = "0x220001aa"

# Largest kernel (Image.gz-dtb) proven to boot on this bootloader with the
# addresses above: the LineageOS 24.0 one. A bigger one is untested territory.
PIONEER_PROVEN_KERNEL_SIZE = "19927413"

# nile-common's BOARD_KERNEL_CMDLINE, then the LuneOS bring-up tokens.
# No selinux=0 (kernel-porting.md § selinux=0) and no swiotlb=1 (fatal on
# athena's 4.19, and not in this board file anyway). The kernel's own
# CONFIG_CMDLINE (EXTEND) goes in FRONT of this string in /proc/cmdline. It is
# NOT the tree's "cgroup_disable=pressure": linux.inc replaces CONFIG_CMDLINE
# with "${CMDLINE} ${CMDLINE_DEBUG}" (" loglevel=3"), and halium-kernel.inc then
# adds its cgroup-v1 switches. So PSI stays enabled here, unlike stock; set
# CMDLINE = "cgroup_disable=pressure" in this recipe to match stock.
ANDROID_BOOTIMG_CMDLINE = "androidboot.hardware=qcom ehci-hcd.park=3 lpm_levels.sleep_disabled=1 sched_enable_hmp=1 sched_enable_power_aware=1 service_locator.enable=1 androidboot.configfs=true androidboot.usbcontroller=a800000.dwc3 loop.max_part=7 androidboot.selinux=permissive printk.devkmsg=on console=tty0"

#-----------------------------------------------------------------------------
# Built with clang, not OE's GCC: LineageOS builds this tree with clang only
# (lineage-24.0's BoardConfigKernel.mk has no GCC path at all), and on a
# Qualcomm tree GCC miscompiles the register-pinned SCM (TrustZone) call - on
# sunfish that took out PIL, the Adreno zap shader, kgsl and with it the whole
# UI (kmi-crc-matching.md § Step 4).
# The compiler family is what matters, not the exact release, so any clang with
# ld.lld and the llvm binutils works; set GKI_CLANG_DIR:pioneer in local.conf
# (e.g. /usr/lib/llvm-19 from the distro's clang-19/lld-19/llvm-19 packages -
# Google's prebuilts are x86_64-only and cannot run on an arm64 build host).
# Wiring copied from linux-google-sunfish.
#-----------------------------------------------------------------------------
GKI_CLANG_DIR ?= ""

python do_check_toolchain() {
    clang_dir = d.getVar("GKI_CLANG_DIR")
    if not clang_dir:
        bb.fatal("GKI_CLANG_DIR is not set. This kernel must be built with clang - "
                 "see the comment in %s." % d.getVar("FILE"))
    for tool in ("clang", "ld.lld", "llvm-ar", "llvm-objcopy"):
        if not os.path.exists(os.path.join(clang_dir, "bin", tool)):
            bb.fatal("GKI_CLANG_DIR %s has no bin/%s" % (clang_dir, tool))
}
addtask check_toolchain before do_configure

PATH =. "${GKI_CLANG_DIR}/bin:"

KERNEL_CC = "clang ${HOST_CC_KERNEL_ARCH} \
 ${@' '.join(f for f in (d.getVar('DEBUG_PREFIX_MAP') or '').split() if f != '-fcanon-prefix-map')} \
 -ffile-prefix-map=${STAGING_KERNEL_DIR}=${KERNEL_SRC_PATH} \
 -ffile-prefix-map=${STAGING_KERNEL_BUILDDIR}=${KERNEL_SRC_PATH} \
"
KERNEL_LD = "ld.lld"
KERNEL_OBJCOPY = "llvm-objcopy"
KERNEL_STRIP = "llvm-strip"

EXTRA_OEMAKE += "CLANG_TRIPLE=aarch64-linux-gnu- LLVM_IAS=1 \
    AR=llvm-ar NM=llvm-nm OBJDUMP=llvm-objdump READELF=llvm-readelf"

#-----------------------------------------------------------------------------
# Config: the three fragments LineageOS selects (TARGET_KERNEL_CONFIG in
# device/sony/pioneer/BoardConfig.mk), then the LuneOS delta. Later lines win
# when kconfig reads the concatenation.
#
# LUNEOS_KERNEL_FRAGMENT = "0" builds LineageOS's config verbatim - the control
# for "is it our delta?" (sunfish, bramble).
#-----------------------------------------------------------------------------
LUNEOS_KERNEL_FRAGMENT ?= "1"

do_configure:prepend() {
    cat ${S}/arch/arm64/configs/vendor/sdm660-perf_defconfig \
        ${S}/arch/arm64/configs/vendor/sony/common.config \
        ${S}/arch/arm64/configs/vendor/sony/pioneer.config > ${WORKDIR}/defconfig
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        cat ${UNPACKDIR}/luneos.cfg >> ${WORKDIR}/defconfig
    fi
}

# Same buildpaths banner rewrite as athena: three generated files record the
# absolute path of their generator, and linux.inc ships the build tree as -src.
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
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c vs C23's constexpr keyword in the host tools (athena, tissot).
BUILD_CFLAGS:append = " -std=gnu17"

# The header, asserted rather than trusted: nothing in bitbake complains about
# a dropped *_RAM_BASE (it silently becomes 0, athena) or an oversized kernel.
# A postfunc, not do_deploy:append: kernel_android writes the .fastboot image in
# its own do_deploy postfunc (android_bootimg_deploy), after any :append has run.
# This one is added after the inherit, so it runs after that.
do_deploy[postfuncs] += "pioneer_check_bootimg"
python pioneer_check_bootimg() {
    import os, struct
    path = os.path.join(d.getVar("DEPLOYDIR"), d.getVar("KERNEL_IMAGETYPE") + ".fastboot")
    with open(path, "rb") as f:
        h = f.read(44)
    if h[:8] != b"ANDROID!":
        bb.fatal("%s: not an Android boot image" % path)
    ks, ka, rs, ra, ss, sa, ta, ps, hv = struct.unpack("<9I", h[8:44])
    bb.note("header v%d; kernel %d B at 0x%08x; ramdisk %d B at 0x%08x; tags 0x%08x; second 0x%08x; page %d"
            % (hv, ks, ka, rs, ra, ta, sa, ps))
    if (hv, ka, ra, ta, sa, ps) != (0, 0x80008000, 0x81000000, 0x80000100, 0x00000000, 4096):
        bb.fatal("boot header differs from the shipped LineageOS boot.img - see the table above")
    if rs == 0:
        bb.fatal("boot image has no ramdisk")
    proven = int(d.getVar("PIONEER_PROVEN_KERNEL_SIZE"))
    if ks > proven:
        bb.fatal("kernel is %d B, larger than the %d B proven on this bootloader" % (ks, proven))
}
