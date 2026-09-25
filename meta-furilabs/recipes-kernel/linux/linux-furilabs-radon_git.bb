require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^radon$"

DESCRIPTION = "Linux kernel for the FuriPhone FLX1s (radon, MT6877 / Dimensity \
900), built from FuriLabs' own published 4.19.325 tree"

# Unlike every other Tier B port in this tree, the source here is not a
# community reconstruction - it is the device vendor's own tree, maintained in
# public and still receiving CVE backports (the repo carries
# feature/forky/CVE-* branches). FuriLabs ship FuriOS, a Debian derivative, on
# this kernel, so it is already a kernel configured for a glibc/systemd
# userspace running an LXC Android container. That is why luneos.cfg is as
# short as it is.

#-----------------------------------------------------------------------------
# Boot image
#-----------------------------------------------------------------------------
# Every value below is DERIVED FROM FuriLabs' own packaging, debian/kernel-info.mk
# in this same repository, which is what builds the boot.img that ships on the
# device. Those are mkbootimg *offsets* against a base; the header fields - and
# so the ANDROID_BOOTIMG_*_RAM_BASE variables, which kernel_android.bbclass
# writes into the header verbatim - are base + offset:
#
#   BASE_OFFSET            0x40078000
#   KERNEL_OFFSET          0x00008000   ->  kernel   0x40080000
#   INITRAMFS_OFFSET       0x11088000   ->  ramdisk  0x51100000
#   TAGS_OFFSET            0x07c08000   ->  tags     0x47c80000
#   DTB_OFFSET             0x07c08000   ->  dtb      0x47c80000
#   SECONDIMAGE_OFFSET     0xbff88000   ->  second   0x100000000 -> 0x00000000
#
# The second-image address is not a typo and not a shortcut. 0x40078000 +
# 0xbff88000 is exactly 2^32, so mkbootimg's 32-bit store of it yields zero;
# there is no second image on this device in any case, and both other legacy
# machines here (athena, mindphone) write 0x00000000. Writing the untruncated
# value is not an option - android_bootimg_v2() packs it with struct "<I" and
# would fail outright.
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "2048"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x40080000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x51100000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x47c80000"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x47c80000"

# The boot-image window (boot-images.md) is 0x51100000 - 0x40080000 =
# 285,736,960 bytes, i.e. 272 MiB between the kernel load address and the
# ramdisk load address. athena's is 16.7 MiB and had to be fought for; here
# there is no constraint worth tracking. Nothing in luneos.cfg is trimmed for
# size on this device, and it does not need to be.

# bootopt is how MediaTek's lk is told the SoC/kernel/userland word sizes;
# 64S3,32N2,64N2 is what FuriLabs pass (KERNEL_BOOTIMAGE_CMDLINE) and it is
# not ours to change.
#
# The rest is LuneOS bring-up instrumentation, and is additive rather than
# replacing: the defconfig sets CONFIG_CMDLINE_EXTEND=y with
#   CONFIG_CMDLINE="root=/dev/ram vmalloc=496M slub_max_order=0 slub_debug=O console=tty0"
# so what the bootloader passes is APPENDED to that, and console=tty0 is
# already in force from the built-in half.
#
# printk.devkmsg=on keeps the dmesg replay out of the ratelimiter, which is how
# early-boot evidence gets collected on a device with no exposed UART.
#
# NOTE: unlike mp01 there is no 20-character cmdline sacrifice here. That
# behaviour belongs to the MT6789 lk, and this is an MT6877 on a completely
# different (4.19-era, non-GKI) bootloader - FuriLabs pass a bare
# "bootopt=..." with nothing padding it and it reaches the kernel intact.
# Do not copy the question marks across.
#
# plymouth.enable=0 is not cosmetic here. This device boots our kernel against
# FuriOS' own bootman initramfs, whose scripts/init-premount/plymouth starts
# plymouthd - and plymouthd survives switch_root still holding the framebuffer.
# LuneOS ships no plymouth units to quit it, so a perfectly healthy LuneOS sat
# invisibly behind a pulsing FuriOS logo, indistinguishable from a hang. That
# cost most of a day: measured 256s, then 566s, then 598s of entirely normal
# uptime behind that logo. Their plymouth script honours this flag, and the only
# two places that restart plymouthd - unlock_encrypted_partition and
# run_furios_recovery - are not reached in this configuration.
#
# firmware_class.path is what makes Wi-Fi and Bluetooth work, and it has to be
# here rather than in a systemd unit.
#
# This kernel builds the MediaTek combo drivers in (CONFIG_MTK_COMBO=y), and in
# that mode they pull their configuration through request_firmware():
# conninfra.cfg for the ConnInfra platform config, wifi.cfg for the wlan tuning.
# The kernel's default search path is /lib/firmware, which does not exist on a
# Halium rootfs - the files live on the vendor partition. Without this the
# requests return -2 and the bring-up fails in a way that reads like a hardware
# fault rather than a missing file:
#
#   conninfra@(conf_parse:511) failed to parse 'coex_wmt_epa_elna'.
#   Fail to get conninfra pinctrl
#   conninfra@(opfunc_subdrv_cal_pwr_on:1225) [opfunc_subdrv_cal_pwr_on] fail [1]
#   wlan 18000000.wifi: Direct firmware load for wifi.cfg failed with error -2
#   [MTK-WIFI] WIFI_write[E]: WMT turn on WIFI fail!
#
# /vendor is already a symlink to /android/vendor in the LuneOS rootfs, so the
# path resolves once mount-android.sh has run - and since the parameter is read
# at each request_firmware() call rather than at boot, it does not matter that
# the mount happens later.
#
# It cannot be deferred to a systemd unit writing
# /sys/module/firmware_class/parameters/path, because there is no second chance:
# conninfra_dev_do_drv_init() guards itself with a static init_done, so once
# conninfra_loader has triggered the init from inside the container (~11s, before
# any of our units), a later retry returns 0 without re-reading anything. The
# firmware has to be findable on the first attempt.
#
# Value copied from FuriLabs' own cmdline on this device, which is the reference
# for "what this kernel expects": firmware_class.path=/vendor/firmware.
ANDROID_BOOTIMG_CMDLINE = "bootopt=64S3,32N2,64N2 plymouth.enable=0 printk.devkmsg=on firmware_class.path=/vendor/firmware"

# The device tree goes in the header's own dtb section (header v2), built by
# this recipe rather than shipped as a prebuilt - this is FuriLabs'
# KERNEL_IMAGE_DTB, arch/arm64/boot/dts/mediatek/mt6877.dtb.
#
# Set through KERNEL_DEVICETREE rather than ANDROID_BOOTIMG_DTB, which is what
# makes kernel.bbclass actually compile the blob; the bootimg class then finds
# it at ${B}/${KERNEL_OUTPUT_DIR}/dts/<this> on its own. ANDROID_BOOTIMG_DTB is
# for a *prebuilt* tree taken from a stock image, which is mindphone's case,
# not ours.
#
# It is a bare FDT here, not a dt_table blob: mindphone needed the 0xd7b7ab1e
# table format because its MT6739 lk only parses that, whereas FuriLabs hand
# mkbootimg the plain mt6877.dtb and that is what boots this hardware.
KERNEL_DEVICETREE = "mediatek/mt6877.dtb"

# 12.0.0, security patch 2025-09 - the values FuriLabs stamp
# (KERNEL_BOOTIMAGE_OS_VERSION / KERNEL_BOOTIMAGE_PATCH_LEVEL), reproduced so
# the bootloader's rollback check sees nothing go backwards. Packed as the
# v2 header wants it, (os_version << 11) | os_patch_level:
#   os_version     = (12<<14) | (0<<7) | 0   = 0x30000
#   os_patch_level = ((2025-2000)<<4) | 9    = 0x199
#   (0x30000 << 11) | 0x199                  = 0x18000199
# NEVER write a lower value than what is already flashed on the device.
ANDROID_BOOTIMG_OS_VERSION = "0x18000199"

# dtbo: FuriLabs build one (KERNEL_IMAGE_WITH_DTB_OVERLAY = 1) and ship it in
# their bootimage package, but explicitly do NOT fold it into the kernel image
# (KERNEL_IMAGE_WITH_DTB_OVERLAY_IN_KERNEL = 0). The dtbo_a/dtbo_b partitions
# on the device keep whatever is already flashed there, which is the stock
# overlay, and that is the right state for a port that changes no board wiring.
# If a future change needs its own overlay, build
# arch/arm64/boot/dts/mediatek/k6877v1_64_k419.dtbo and flash it separately -
# it does not belong in this boot image.

# Restore the kernel's own built-in command line.
#
# meta-oe's linux.inc does, unconditionally:
#
#   CMDLINE_DEBUG ?= "loglevel=3"
#   kernel_conf_variable CMDLINE "\"${CMDLINE} ${CMDLINE_DEBUG}\""
#
# i.e. it REPLACES CONFIG_CMDLINE with "${CMDLINE} ${CMDLINE_DEBUG}". With
# CMDLINE unset that is " loglevel=3", and the defconfig's own string is gone -
# verified on the first build here, where the built .config came out as
# " loglevel=3 systemd.unified_cgroup_hierarchy=0 ..." with nothing of
# MediaTek's left. halium-kernel.inc appends to whatever survives that, so it
# does not put it back.
#
# Set CMDLINE to the stock string so the built-in half matches what FuriLabs
# ship. The one that actually matters is console=tty0, which the framebuffer
# console in luneos.cfg depends on; vmalloc= is inert on arm64 (it is an arm32
# early param) and root=/dev/ram is moot with an initramfs, but they are the
# vendor's and cost nothing.
#
# The final built-in line is therefore this, plus " loglevel=3", plus
# halium-kernel.inc's systemd cgroup-v1 pair - and then the boot image's own
# ANDROID_BOOTIMG_CMDLINE on top of that, because CONFIG_CMDLINE_EXTEND=y.
CMDLINE = "root=/dev/ram vmalloc=496M slub_max_order=0 slub_debug=O console=tty0"

LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"

#-----------------------------------------------------------------------------
# Toolchain: Google's prebuilt Clang, as sunfish, q25, mp01 and bramble do
#-----------------------------------------------------------------------------
# This tree has never been built with anything but Clang. FuriLabs' own
# packaging pins it exactly - debian/kernel-info.mk says
#
#   BUILD_CC   = clang
#   BUILD_LLVM = 1
#   BUILD_PATH = /usr/lib/llvm-android-12.0-r416183b/bin
#
# i.e. Android Clang r416183b, which is the SAME release q25, mp01, bramble and
# sunfish already pin, so it is on disk already:
#
#   GKI_CLANG_DIR:radon = "/home/herrie/Documents/GitHub/luneos-gki-toolchain-android12-5.10/prebuilts-master/clang/host/linux-x86/clang-r416183b"
#
# Put that in local.conf next to the others. do_check_toolchain below fails
# early and legibly rather than letting a GCC build get halfway.
#
# Modelled on linux-google-sunfish_git.bb rather than on q25: sunfish is the
# other machine here that keeps kernel.bbclass and kernel_android (because it
# too needs a header v2 boot image with a dtb section, which
# gki_bootimg.bbclass cannot write) and swaps only the toolchain. q25's
# wholesale KERNEL_MAKE override belongs to the GKI path and is not wanted here.
GKI_CLANG_VERSION ?= "r416183b"
GKI_CLANG_DIR ?= ""

python do_check_toolchain() {
    clang_dir = d.getVar("GKI_CLANG_DIR")
    if not clang_dir:
        bb.fatal("GKI_CLANG_DIR is not set. This kernel must be built with "
                 "Google's Clang %s - see the comment in %s."
                 % (d.getVar("GKI_CLANG_VERSION"), d.getVar("FILE")))
    if not os.path.exists(os.path.join(clang_dir, "bin", "clang")):
        bb.fatal("GKI_CLANG_DIR %s has no bin/clang" % clang_dir)
}
addtask check_toolchain before do_configure

# Prepended for every task rather than exported in each one: do_configure,
# do_compile and do_compile_kernelmodules all invoke the compiler.
PATH =. "${GKI_CLANG_DIR}/bin:"

# kernel-arch.bbclass builds these out of ${HOST_PREFIX}gcc. Keep everything it
# puts in them - the -ffile-prefix-map pair is what keeps build paths out of the
# -src package and the buildpaths QA check green - and swap the tools.
# DEBUG_PREFIX_MAP is filtered rather than used as-is: OE puts
# -fcanon-prefix-map in it, which is a GCC 12+ option this Clang does not know
# ("clang-12: error: unknown argument: '-fcanon-prefix-map'"), while the
# -ffile-prefix-map / -fdebug-prefix-map / -fmacro-prefix-map entries beside it
# are understood and are what keep build paths out of the packages.
KERNEL_CC = "clang ${HOST_CC_KERNEL_ARCH} \
 ${@' '.join(f for f in (d.getVar('DEBUG_PREFIX_MAP') or '').split() if f != '-fcanon-prefix-map')} \
 -ffile-prefix-map=${STAGING_KERNEL_DIR}=${KERNEL_SRC_PATH} \
 -ffile-prefix-map=${STAGING_KERNEL_BUILDDIR}=${KERNEL_SRC_PATH} \
"
KERNEL_LD = "ld.lld"
KERNEL_OBJCOPY = "llvm-objcopy"
KERNEL_STRIP = "llvm-strip"

# What kbuild needs on top of CC/LD, which kernel.bbclass does not pass:
#
#   CLANG_TRIPLE          4.19 errors out with "Clang with Android --target
#                         detected. Did you specify CLANG_TRIPLE?" if the triple
#                         ends up an Android one; it derives --target from this.
#   LLVM_IAS=1            use Clang's integrated assembler, so no GNU as is
#                         needed for the arm64 inline asm.
#   AR/NM/...             without LLVM=1 kbuild reaches for $(CROSS_COMPILE)ar
#                         and friends; the llvm-* equivalents ship in the same
#                         prebuilt.
#   CROSS_COMPILE_COMPAT  CONFIG_COMPAT_VDSO=y in this defconfig (verified), so
#                         the 32-bit vDSO is built too and needs its own triple.
#                         Same as q25; sunfish does not need it.
#
# NOT passed, unlike q25: PYTHON=python3. That is a 5.10 problem -
# link-vmlinux.sh there calls ${PYTHON} scripts/jobserver-exec. Checked against
# this tree's scripts/link-vmlinux.sh: no PYTHON reference, no jobserver-exec.
EXTRA_OEMAKE += "CLANG_TRIPLE=aarch64-linux-gnu- LLVM_IAS=1 \
    CROSS_COMPILE_COMPAT=arm-linux-gnueabi- \
    AR=llvm-ar NM=llvm-nm OBJDUMP=llvm-objdump READELF=llvm-readelf"

# Stock CONFIG_LTO_CLANG=y is KEPT - see luneos.cfg. With the vendor's own
# compiler there is no reason to build the kernel differently from the way it
# is validated.
#-----------------------------------------------------------------------------

inherit kernel_android pkgconfig

# The boot image takes its ramdisk from INITRAMFS_NAME, which radon.conf points
# at FuriOS' prebuilt bootman initramfs rather than initramfs-android-image (see
# the long comment there for why). kernel_android.bbclass only knows to wait for
# initramfs-android-image, so name the other producer explicitly or do_deploy
# races it and fails with "Required initramfs image ... is not available".
# Set HERE and not in radon.conf: kernel_android.bbclass assigns INITRAMFS_NAME
# unconditionally (it has to - kernel.bbclass sets it first), and bbclasses are
# parsed after machine.conf, so a machine-level setting is silently discarded.
# A recipe-level assignment after the inherit is the only one that survives.
# Boot with FuriOS' bootman initramfs, not ours.
#
# SETTLED ON HARDWARE (24 Sep 2026). Our Halium initramfs never reached the
# rootfs on this device; our kernel paired with FuriLabs' ramdisk boots it, and
# that is the combination that produced the first working UI. The reason is
# structural rather than a bug to chase: FuriOS keeps each installed OS in an LVM
# logical volume, repurposes vendor_boot_a as its persist store, and switches
# between them with bootman - and LuneOS' initramfs (Tofee's tofe/halium-9.0)
# contains no LVM support whatsoever, while FuriOS' has ~26 LVM code paths.
# Their script also lays the rootfs out exactly the way LuneOS expects
# (/halium-system/var/lib/lxc/android/android-rootfs.img, /android,
# /userdata/android-data), so nothing is lost by using it.
#
# The working Ubuntu Touch port for this device does the same thing and builds no
# initramfs of its own either - deviceinfo-radon just sets
# deviceinfo_prebuilt_boot_ramdisk.
#
# initramfs-android-image is still built (kernel_android.bbclass depends on it
# unconditionally); this only changes which file ends up in the boot image.
INITRAMFS_NAME = "initramfs-furios-bootman-${MACHINE}.cpio.gz"

do_deploy[depends] += "furios-bootman-initramfs:do_deploy"

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/furilabs/linux-furiphone-radon.git;branch=forky;protocol=https \
           file://luneos.cfg \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# furilabs/linux-furiphone-radon forky, "defconfig: bump CMA to 64",
# 7 Sep 2026. "forky" is FuriLabs' release branch name (Debian forky), not a
# device name - the same branch name is used across all their repositories.
SRCREV = "5144e9240df3126e011c114e50f0b18e6d54b64d"

LINUX_VERSION = "4.19"
KV = "4.19.325"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native openssl-native"

# k6877v1_64_k419_defconfig is the config FuriLabs' own packaging selects
# (KERNEL_DEFCONFIG in debian/kernel-info.mk) - the single defconfig in
# arch/arm64/configs, 7,514 lines. luneos.cfg is the LuneOS delta on top of it;
# later lines win over earlier ones when kconfig reads the concatenation.
do_configure:prepend() {
    cat ${S}/arch/arm64/configs/k6877v1_64_k419_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# Optional initramfs debug shell, off by default. With it, init.sh stops and
# starts adbd inside the initramfs instead of continuing the boot, which is the
# only way to read early boot state on a device with no exposed UART.
#
#   MACHINE=radon LUNEOS_ENABLE_ADB=1 bitbake linux-furilabs-radon
#
# (after adding LUNEOS_ENABLE_ADB to BB_ENV_PASSTHROUGH_ADDITIONS, or set it in
# local.conf - staging/radon-staging/make-debug-boot.sh does it with a -R conf
# fragment so neither is needed.)
#
# Built into CONFIG_CMDLINE rather than added to ANDROID_BOOTIMG_CMDLINE, which
# would be one line and no recompile. The reason is that init.sh greps
# /proc/cmdline, and whether this bootloader passes the boot image's command
# line to the kernel at all is UNVERIFIED: MediaTek's own bootopt= is consumed
# by lk itself, so FuriLabs using it proves nothing, and the MT6789 lk on mp01
# is known to eat the first 20 characters of it. CONFIG_CMDLINE_EXTEND puts the
# built-in string in front of whatever the bootloader supplies, and the
# bootloader cannot filter that. Same conclusion as sargo and sunfish reached
# for a different bootloader.
#
# LUNEOS_ENABLE_ADB is read inside do_configure, so bitbake's signature for
# that task changes with it and the kernel rebuilds on its own - no
# cleansstate, and the normal image stays in sstate for the way back.
LUNEOS_ENABLE_ADB ??= "0"

do_configure:append() {
    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
        oe_runmake olddefconfig
    fi
}

# Three files kbuild generates into ${B} record the absolute path of the tool
# or input they were made from, in a banner comment. linux.inc ships the build
# tree as the -src package, so those paths go out with it and wrynose fails
# do_package_qa with "contains reference to TMPDIR [buildpaths]". Rewrite the
# banners to name the files the way an in-tree build would; they are comments,
# and the generated data below them is unchanged. Same treatment as athena.
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
    # linux.inc ships everything under ${exec_prefix}/src/linux* as
    # kernel-headers. Those files record absolute command lines, which wrynose
    # rejects as "contains reference to TMPDIR [buildpaths]".
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
