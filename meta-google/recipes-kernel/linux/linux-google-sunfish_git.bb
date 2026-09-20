require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs. Most of it
# is already right in the stock sunfish defconfig (BINDERFS, VETH and
# OVERLAY_FS are all on), but the options there are forced rather than
# appended, so requiring it is a cheap way to stay consistent with every other
# waydroid-capable device instead of relying on a defconfig happening to agree.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^sunfish$"

DESCRIPTION = "Linux kernel for the Google Pixel 4a (sunfish), based on the \
LineageOS msm-4.14 tree"

# The kernel source. LineageOS keeps sunfish on the shared Google msm-4.14
# tree (kernel/google/msm-4.14 in its manifest, device/google/sunfish's
# BoardConfigLineage.mk names sunfish_defconfig against it) and still updates
# it; the device-specific android_kernel_google_sunfish repo was abandoned at
# lineage-18.1 / 4.14.212 and should not be used.
SRC_URI = "git://github.com/LineageOS/android_kernel_google_msm-4.14.git;branch=lineage-22.2;protocol=https \
           file://luneos.cfg \
           "
SRCREV = "b8cf65288a7ae059b951c8f00051a4b6dbeba9f8"

LINUX_VERSION = "4.14.355"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# lz4-native for Image.lz4-dtb: this tree's cmd_lz4 shells out to "lz4c -9",
# and lz4-native stages lz4c (q25, mp01 and linux-halium-gki already rely
# on it here).
DEPENDS += "dtc-native python3-dtschema-wrapper-native openssl-native lz4-native"

inherit kernel_android pkgconfig

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# Everything below is read out of the *stock* boot.img
# (sunfish-tq3a.230805.001.s2, the final Pixel 4a OTA), not out of
# device/google/sunfish/BoardConfig-common.mk - the two disagree, and the
# shipped image wins. Dump it again with the snippet in the notes at the
# bottom of this file if a later OTA is ever used as the reference.
#
#   header_version 2, page_size 4096, header_size 1660
#   kernel_addr    0x00008000     (= BOARD_KERNEL_BASE + 0x8000)
#   ramdisk_addr   0x01000000     board file says BOARD_RAMDISK_OFFSET
#                                 0x02000000; the shipped image does not
#   tags_addr      0x00000100     board file says 0x01E00000; likewise
#   second_addr    0x00000000     no second stage, size 0
#   dtb_addr       0x01f00000     dtb section is one 425,348 byte FDT
#
# The two disagreements are the whole reason for pulling the image: a boot
# image is one of the few things here that cannot be debugged from the sofa,
# and both wrong values would have been silently accepted by the build.
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x01f00000"

# Stock's value, decoded: (13 << 25) | (23 << 4) | 8 - Android 13.0.0, security
# patch 2023-08, which is what TQ3A.230805.001 is. Nothing on a Pixel is known
# to read this field (AVB does rollback protection through vbmeta, not here),
# but matching stock costs nothing and mindphone's MTK lk did check its
# equivalent.
ANDROID_BOOTIMG_OS_VERSION = "0x1a000178"

# The boot-image window, athena's rule, and it is *tight* here: the kernel
# loads at 0x8000 and the ramdisk at 0x01000000, so everything before the
# ramdisk has 16 MiB to fit in. The stock lz4 kernel is 15,436,575 bytes -
# 1.25 MiB of headroom, and the luneos.cfg delta eats into it. That is why
# this recipe matches stock's lz4 compression rather than switching to gzip,
# and why do_deploy checks the window below instead of trusting the build:
# nothing in bitbake complains about a kernel that overlaps its own initramfs.

# Stock's command line, minus two things:
#
#   lpm_levels.sleep_disabled=1   BoardConfig-common.mk carries it with a
#                                 #STOPSHIP marker. It disables every SoC
#                                 low-power mode; on sargo that left cpuidle
#                                 C1-C3 usage at exactly zero after hours of
#                                 uptime. With LPM on, the deep C-states
#                                 engage within seconds.
#   androidboot.*                 kept, because the container reads them.
#   buildvariant=user             stock carries it; dropped here, as sargo
#                                 does, so the container is not told it is a
#                                 user build.
#
# firmware_class.path=/vendor/firmware is the one addition - it is not in the
# stock line. sargo carries it for the same reason: force-loaded vendor
# modules look for their firmware through it.
#
# Nothing LuneOS-specific is added here on purpose: a Pixel bootloader passes
# on only the arguments it already recognises and silently drops the rest -
# measured on sargo with a "zz.marker=1" canary. Anything we need goes into
# CONFIG_CMDLINE instead, which is what halium-kernel.inc does for the
# cgroup-v1 switches.
#
# No datapart= either: init.sh finds userdata by PARTNAME out of sysfs
# (find_partition_by_name), which is what makes a UFS device with no fixed
# mmcblk numbering work at all.
ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM0,115200n8 androidboot.console=ttyMSM0 printk.devkmsg=on msm_rtb.filter=0x237 ehci-hcd.park=3 service_locator.enable=1 androidboot.memcg=1 cgroup.memory=nokmem usbcore.autosuspend=7 loop.max_part=7 loop.hw_queue_depth=31 androidboot.usbcontroller=a600000.dwc3 swiotlb=1 androidboot.boot_devices=soc/1d84000.ufshc cgroup_disable=pressure firmware_class.path=/vendor/firmware"

# The stock defconfig plus the LuneOS delta. Later lines win when kconfig
# reads the concatenation, so luneos.cfg goes second - same arrangement as
# mindphone.
do_configure:prepend() {
    cat ${S}/arch/arm64/configs/sunfish_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

do_configure:append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${B}/.config
      kernel_conf_variable $1 $2 ${B}/.config
  }

# fixup some options which get changed from Y to M in oldconfig :/
  kernel_conf_variable_fixup USB_LIBCOMPOSITE y
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

  # The optional initramfs debug shell; see LUNEOS_ENABLE_ADB below. One
  # do_configure:append for the whole recipe: bitbake would concatenate
  # several, but a single function keeps the order of these steps - and the
  # olddefconfig that resolves them - in one place.
  if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
      halium_kernel_add_cmdline "enable_adb"
  fi

  oe_runmake olddefconfig
}

# Optional initramfs debug shell, off by default.
#
# The usual way in - an "enable_adb" boot image - does not work on a Pixel:
# the bootloader drops command line arguments it does not recognise, so
# nothing added to ANDROID_BOOTIMG_CMDLINE reaches /proc/cmdline, and init.sh
# greps /proc/cmdline for enable_adb. The flag therefore has to be built into
# the kernel. Same as sargo; see the note in halium-kernel.inc.
#
#   MACHINE=sunfish LUNEOS_ENABLE_ADB=1 bitbake linux-google-sunfish
#
# after adding LUNEOS_ENABLE_ADB to BB_ENV_PASSTHROUGH_ADDITIONS, or set it in
# local.conf.
LUNEOS_ENABLE_ADB ??= "0"

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;" and assigns to it. C23
# made constexpr a keyword and this host GCC (15.x) defaults to -std=gnu23, so
# the kernel host tools fail to build:
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#
# Upstream renamed the variable in 6.7; this tree predates that. Only the host
# tools are affected - the kernel itself uses OE's cross toolchain - and
# kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host C
# standard is enough. Hit on every pre-6.7 tree here: sargo (4.9), tissot
# (4.9) and mindphone (4.14).
BUILD_CFLAGS:append = " -std=gnu17"

#-----------------------------------------------------------------------------
# Two things this recipe deliberately does NOT do, both of which want checking
# on the first real device:
#
# 1. The dtbo partition is left stock.
#
#    sunfish's kernel is built with CONFIG_BUILD_ARM64_DT_OVERLAY=y: the board
#    description is eight *overlays* (sm7150-sunfish-{dev,evt,dvt,pvt,mp}*.dtbo)
#    applied by the bootloader onto a base SoC tree, qcom-base/sdmmagpie.dtb.
#    Only the base ones end up appended to Image.lz4-dtb, so that is what lands
#    in the v2 dtb section here - exactly the stock split, where the overlays
#    live in their own partition.
#
#    The catch is that the pair has to agree: the stock dtbo blobs were built
#    against Google's base tree and will be applied to ours. The stock dtbo.img
#    pulled from TQ3A.230805.001.s2 supports the expectation that they match:
#    8 entries of ~250 KB, which is exactly the eight
#    sm7150-sunfish-{dev1.0,dev1.1,dev1.2,evt1.0,evt1.1,dvt1.0,pvt1.0,mp1.0}
#    overlays this kernel tree builds. Their id fields are all zero, so the
#    bootloader selects by the qcom,board-id/msm-id properties inside each FDT
#    rather than by the table header - which is also why a base dtb from a
#    different build still gets the right overlay. Expected to work; not proven.
#
#    If the device does not boot at all with a kernel that is otherwise sane,
#    build the matching dtbo.img from this tree and flash it:
#
#        make O=... sunfish_defconfig && make O=... dtbo.img
#
#    That target needs AOSP's `mkdtimg` (system/libufdt) on PATH, which OE does
#    not package - which is why it is not wired up here.
#
# 2. Nothing is done about the stock vendor modules.
#
#    init.insmod.sunfish.cfg modprobes ~40 .ko files out of /vendor/lib/modules
#    (the whole audio dlkm stack, ftm5 touch, drv2624 haptics, wlan.ko, ...) and
#    the stock defconfig sets CONFIG_MODVERSIONS, so their CRCs are checked. A
#    kernel rebuilt with the luneos.cfg delta moves those CRCs and they will be
#    refused. luneos.cfg therefore sets CONFIG_MODULE_FORCE_LOAD=y, which is
#    what made this work on mindphone (wifi, BT and GPS all came up force-loaded)
#    - but the vendor's own init.insmod.sh calls plain `modprobe`, with no
#    --force, so on this device the force has to come from the host side
#    (modprobe --force-vermagic out of /android/vendor/lib/modules after
#    android-system.service has mounted vendor, the way mtk-load-modules.sh does
#    it for mindphone) or not at all.
#
#    Two pieces of good news measured off the trees rather than assumed:
#      - init.insmod.sh sets vendor.all.modules.ready / vendor.all.devices.ready
#        unconditionally at the end, *even when every modprobe failed*, so the
#        `wait_for_prop vendor.all.modules.ready 1` gate in init.hardware.rc
#        cannot wedge boot the way sargo's vendor.qcom.time.set did.
#      - LineageOS builds most of that list in: of the whole cfg line only nine
#        symbols are =m in sunfish_defconfig (qcacld wlan, heatmap, drv2624,
#        msm_11ad, rdbg, llcc_perfmon, mpq, incremental-fs). The touchscreen
#        (CONFIG_TOUCHSCREEN_FTS_S5) and the audio stack are built-in, so a
#        first boot should have a display, touch and audio even if not one
#        stock module loads. Wifi is the one that really needs the modules.
#-----------------------------------------------------------------------------

# Fail the build rather than ship a boot image whose kernel runs into its own
# ramdisk. athena's rule (boot-images.md): the kernel is loaded at
# ANDROID_BOOTIMG_KERNEL_RAM_BASE and everything before
# ANDROID_BOOTIMG_RAMDISK_RAM_BASE has to fit below it. On sunfish that is
# 16 MiB against a stock kernel of 15,436,575 bytes, so the margin is about
# 1.25 MiB and the luneos.cfg delta spends some of it. Nothing in bitbake
# notices this on its own, and the failure it produces on the device is a
# silent non-boot.
#
# Runs after android_bootimg_deploy, which is the postfunc that assembles
# boot.img - postfuncs run in the order they are appended.
do_deploy[postfuncs] += "sunfish_check_boot_window"

python sunfish_check_boot_window() {
    import os, struct

    bootimg = os.path.join(d.getVar("B"), "boot.img")
    if not os.path.exists(bootimg):
        bb.fatal("sunfish_check_boot_window: %s was never assembled" % bootimg)

    with open(bootimg, "rb") as f:
        hdr = f.read(64)
    if hdr[:8] != b"ANDROID!":
        bb.fatal("sunfish_check_boot_window: %s is not an Android boot image" % bootimg)

    kernel_size, kernel_addr = struct.unpack_from("<II", hdr, 8)
    ramdisk_size, ramdisk_addr = struct.unpack_from("<II", hdr, 16)

    end = kernel_addr + kernel_size
    if end >= ramdisk_addr:
        bb.fatal("sunfish_check_boot_window: the kernel ends at 0x%x and the "
                 "ramdisk loads at 0x%x - it would be overwritten by its own "
                 "initramfs. Trim the config (see luneos.cfg) rather than "
                 "moving the load address: 0x%x is what the stock image uses."
                 % (end, ramdisk_addr, ramdisk_addr))

    bb.note("sunfish boot window: kernel %d B ends at 0x%x, ramdisk (%d B) "
            "loads at 0x%x - %d B of headroom"
            % (kernel_size, end, ramdisk_size, ramdisk_addr, ramdisk_addr - end))
}

# How the reference numbers above were obtained, for the next OTA:
#
#   unzip -j sunfish-<build>-factory-<hash>.zip '*/image-sunfish-*.zip'
#   unzip -j image-sunfish-<build>.zip boot.img dtbo.img
#   python3 -c 'import struct;b=open("boot.img","rb").read();\
#     print([hex(x) for x in struct.unpack_from("<IIIIIIIII",b,8)])'
#
# The reference images for TQ3A.230805.001.s2 are kept outside the build tree,
# in ~/webos/LuneOS/sunfish/stock/.
