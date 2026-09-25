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
SRC_URI = "git://github.com/LineageOS/android_kernel_google_msm-4.14.git;branch=lineage-23.2;protocol=https \
           file://luneos.cfg \
           file://lineage-config \
           file://0002-techpack-qcacld-drop-Werror-from-the-vendor-Kbuilds.patch \
           "
# Pinned to the revision the LineageOS 23.2 nightly for sunfish was built from,
# taken out of that build's own build-manifest.xml:
#
#   LineageOS/android_kernel_google_msm-4.14  28f9290ae067d3e7857126eb6312720dbe9c7c80
#
# LOS 23.2 is the current, actively-maintained release for this device (latest
# nightly 2026-09-17), and the target is to match it - not the /e/OS 13 build
# the phone happened to arrive with.
#
# An earlier revision aimed at the latter instead, pinning fe61ffb52659: the
# modules the owner pulled off the phone name it directly,
#
#   vermagic=4.14.336-gfe61ffb52659 SMP preempt mod_unload modversions aarch64
#
# and that is the head of lineage-20 (Android 13, matching the vendor API
# level 33 the device reports). The point was to keep the phone's own prebuilt
# vendor modules loadable. That goal is gone: this recipe now builds the vendor
# modules itself from this same tree, so they match by construction and the
# revision is free to follow the distribution we actually want to track. Kept
# here because the number is otherwise unrecoverable once the phone is
# reflashed.
#
# For the record from that round: lineage-22.2 (4.14.355), which this recipe
# used before either pin, scored 42 of 42 stock modules failing to load.
SRCREV = "28f9290ae067d3e7857126eb6312720dbe9c7c80"

LINUX_VERSION = "4.14.357"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

# lz4-native for Image.lz4-dtb: this tree's cmd_lz4 shells out to "lz4c -9",
# and lz4-native stages lz4c (q25, mp01 and linux-halium-gki already rely
# on it here).
DEPENDS += "dtc-native python3-dtschema-wrapper-native openssl-native lz4-native"

inherit kernel_android pkgconfig

# Left EMPTY on purpose. kernel.bbclass exports LOCALVERSION from this, and
# meta-oe's linux.inc then also writes CONFIG_LOCALVERSION="${LOCALVERSION}" -
# and scripts/setlocalversion appends *both*, so any value here lands twice:
#   4.14.357-openela-g28f9290ae067-g28f9290ae067
# The suffix is set once, in do_configure:append below, after linux.inc has had
# its say.
KERNEL_LOCALVERSION = ""

#-----------------------------------------------------------------------------
# Built with Google's Clang, not OE's GCC - and that is a fix, not a preference.
#
# With the GCC build, every single TrustZone call failed on hardware:
#
#   scm_call failed with error code -1                    (x44)
#   QSEECOM: qseecom_probe: Failed to get QSEE version info -19
#   scm_enable_mem_protection: SCM call failed
#
# and with SCM dead nothing that needs the secure world works: PIL could not
# authenticate a single peripheral image (modem, cdsp, venus, npu and ipa_fws
# all "loading from 0x... -> Initializing image failed(rc:-5)"), and because
# the Adreno zap shader loads through that same path, kgsl never bound, there
# was no /dev/kgsl-3d0, EGL could not initialise and surface-manager aborted in
# a loop. One root cause, the whole device.
#
# The arm64 SCM entry in drivers/soc/qcom/scm.c is register-pinned inline
# assembly - "register u64 rN asm(...)" plus __asmeq - which is exactly the
# construct where GCC and Clang disagree, and this tree has never been built
# with anything but Clang: build.config.common names
# prebuilts-master/clang/host/linux-x86/clang-r416183b, and the stock kernel's
# own banner says "Android (7284624, based on r416183b) clang version 12.0.5".
# That is the same release q25, mp01 and bramble already pin, so it is on disk
# already; point GKI_CLANG_DIR:sunfish at it in local.conf.
#
# Unlike bramble, this recipe keeps kernel.bbclass and kernel_android: sunfish
# needs a header v2 boot image with a dtb section, which gki_bootimg.bbclass
# cannot write. So only the toolchain changes here - the boot image, the
# packaging and the QA fixes all stay as they were.
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
# -fuse-ld=bfd goes with the GCC driver and is replaced by ld.lld below.
# DEBUG_PREFIX_MAP is filtered rather than used as-is: OE puts
# -fcanon-prefix-map in it, which is a GCC option (12+) that this Clang does
# not know -
#
#   clang-12: error: unknown argument: '-fcanon-prefix-map'
#
# while the -ffile-prefix-map / -fdebug-prefix-map / -fmacro-prefix-map pairs
# it also contains are understood and are what keep build paths out of the
# packages.
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
#   CLANG_TRIPLE   this Makefile errors out with "Clang with Android --target
#                  detected. Did you specify CLANG_TRIPLE?" if the triple ends
#                  up an Android one; it derives --target from this.
#   LLVM_IAS=1     use Clang's integrated assembler, so no GNU as is needed for
#                  the arm64 inline asm.
#   AR/NM/...      without LLVM=1 kbuild reaches for $(CROSS_COMPILE)ar and
#                  friends; the llvm-* equivalents ship in the same prebuilt.
EXTRA_OEMAKE += "CLANG_TRIPLE=aarch64-linux-gnu- LLVM_IAS=1 \
    AR=llvm-ar NM=llvm-nm OBJDUMP=llvm-objdump READELF=llvm-readelf"
#-----------------------------------------------------------------------------


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
# 0x02000000, which is BOARD_RAMDISK_OFFSET in device/google/sunfish's
# BoardConfig-common.mk - i.e. what LineageOS builds this device's boot images
# with - and NOT the 0x01000000 the stock Google image uses.
#
# That looks like ignoring the measurement, so: the stock image really does say
# 0x01000000, and an earlier revision of this recipe followed it on the
# principle that the shipped image beats the board file. But this device runs
# LineageOS's vendor, so the kernel has to carry LineageOS's config - full LTO
# and CFI - and that kernel does not fit under a 16 MiB ramdisk address. It fits
# under 32 MiB, which is precisely why the board file says 32 MiB and why
# LineageOS's own boot images boot on this phone.
#
# Both values are in use in the wild on sunfish, so this is not a case of one
# being wrong; it is a case of the ramdisk address belonging with the kernel it
# was chosen for.
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x02000000"
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

# The device trees, and why neither of these is optional.
#
# DTC_EXT: this tree's in-tree dtc is 1.4.4, which cannot parse the overlay
# syntactic sugar the sunfish overlays are written in - a bare "&thermal_zones
# {" at the top level of a /plugin/ file:
#
#   Error: qcom-base/sdmmagpie-thermal-overlay.dtsi:15.1-15 syntax error
#   FATAL ERROR: Unable to parse input tree
#
# That is not a local problem: the vendor never uses the in-tree one either.
# build.config.common in this same tree exports
# DTC_EXT=prebuilts/kernel-build-tools/linux-x86/bin/dtc, a newer dtc, and
# every Google build of these overlays goes through it. OE's dtc-native is
# v1.7.2+ and parses them (checked against a minimal /plugin/ reduction of the
# failing construct), so point kbuild at that instead.
#
# DTC_FLAGS=-@: the base dtb has to carry __symbols__, or the stock dtbo
# overlays have nothing to resolve their fixups against and the bootloader
# produces a device tree that does not describe this board. Verified against
# the stock image rather than assumed - the 425,348 byte FDT in
# sunfish-tq3a.230805.001.s2's boot.img contains __symbols__ and no
# __fixups__, which is exactly what "-@ on the base, /plugin/ on the overlays"
# produces. Exported rather than passed on the make command line so that
# Makefile.lib's own "DTC_FLAGS += -q -Wno-unit_address_vs_reg" still appends
# instead of being overridden.
export DTC_EXT = "${STAGING_BINDIR_NATIVE}/dtc"
export DTC_FLAGS = "-@"

# The stock defconfig plus the LuneOS delta. Later lines win when kconfig
# reads the concatenation, so luneos.cfg goes second - same arrangement as
# mindphone.
do_configure:prepend() {
    # The base is the config the shipped LineageOS 23.2 kernel actually carries,
    # extracted from the IKCFG_ST blob in that build's boot.img - not
    # arch/arm64/configs/sunfish_defconfig. The defconfig is what LineageOS
    # feeds kbuild; the file below is what came out, with every dependency
    # already resolved. Since this device runs LineageOS's own vendor modules
    # and CONFIG_MODVERSIONS is on, matching what they built beats matching
    # what they asked for - which is the lesson from two rounds of chasing
    # DEBUG_FS and CFI through the defconfig.
    # LUNEOS_KERNEL_FRAGMENT = "0" builds LineageOS's config verbatim, with no
    # LuneOS delta at all. That is the control for the CRC question: if the
    # verbatim build's modules load and ours do not, the difference is in our
    # 37-option delta and can be bisected. bramble's recipe has had this switch
    # from the start; sunfish's did not, which invalidated one earlier
    # "baseline" run of mine that silently still carried the whole delta.
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        cat ${UNPACKDIR}/lineage-config ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
    else
        cp ${UNPACKDIR}/lineage-config ${WORKDIR}/defconfig
    fi
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

  # Undo three things meta-oe's linux.inc forces on every kernel it builds
  # (see its kernel_conf_variable block). They have to be corrected *here*
  # rather than in a config fragment, because that block runs after the
  # fragment has been merged.
  #
  # BLK_DEV_BSG is the one that matters, and it is why 5 of LineageOS 23.2's
  # 41 vendor modules would not load even from a verbatim build of their own
  # config: it adds bsg_dev to struct request_queue, which struct inode reaches
  # through i_bdev -> bd_queue and task_struct reaches through its file tables,
  # so every symbol mentioning either gets a different genksyms CRC -
  # cdev_add/cdev_init/ihold/generic_read_dir for struct inode,
  # wake_up_process/__put_task_struct/set_cpus_allowed_ptr for task_struct.
  # Nothing in the kernel mandates it (default y, and the only thing that
  # selects it, BLK_DEV_BSGLIB, is off); linux.inc turns it on for a udev
  # claim in its Kconfig help text that does not apply here - LuneOS finds
  # partitions by GPT name through blkid, not through /dev/bsg. LineageOS
  # ships it off, so we ship it off.
  kernel_conf_variable_fixup BLK_DEV_BSG n

  # The other half of the vermagic, set once and after linux.inc: with
  # KERNEL_LOCALVERSION empty the env LOCALVERSION is empty too, so
  # setlocalversion appends only this.
  kernel_conf_variable_fixup LOCALVERSION "\"-g28f9290ae067\""

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
LUNEOS_KERNEL_FRAGMENT ??= "1"

LUNEOS_ENABLE_ADB ??= "0"

# crypto/jitterentropy.c is compiled -O0 on purpose (crypto/Makefile:
# "CFLAGS_jitterentropy.o = -O0"), so the compiler cannot optimise away the
# timing variations the driver measures. At -O0 Clang materialises the __FILE__
# of the BUG/WARN macro next to jent_panic() as a plain string constant in
# .rodata.str1.1 - verified with llvm-readelf, it sits directly before
# "jitterentropy: Duplicate output detected." - and a string constant is past
# the point where -ffile-prefix-map or -fmacro-prefix-map can rewrite it. Both
# are already passed and neither removes it. GCC folded it away, which is why
# this appears only with the toolchain switch.
#
# It lands in kernel-vmlinux, which is a debug package: nothing installs it
# (it appears in no image manifest and no packagegroup), so the build path
# never reaches a device. Exclude that ONE package from the buildpaths check
# rather than the whole recipe - kernel, kernel-modules, kernel-headers and
# the -src package all stay under it, and those are the ones that ship.
INSANE_SKIP:kernel-vmlinux += "buildpaths"

do_compile:append() {
    # Three files kbuild generates during the build record the absolute path of
    # their input in a comment, and linux.inc ships the build tree as -src, so
    # wrynose rejects the package:
    #
    #   consolemap_deftbl.c   " * conmakehash <abs>/drivers/tty/vt/cp437.uni > [this file]"
    #   logo_linux_clut224.c  " *  It was automatically generated from <abs>/...ppm"
    #   oid_registry_data.c   " * Automatically generated by <abs>/lib/build_OID_registry."
    #
    # -ffile-prefix-map cannot help: these are strings the generator scripts
    # print, not debug info. Rewrite them to the relative path an in-tree build
    # would have produced - the same treatment mindphone gives its generated
    # mach-types.h banner, and the same class of problem as the ..install.cmd
    # files removed in do_install below.
    #
    # Two of the three are ours: CONFIG_VT (luneos.cfg - systemd wants a console)
    # brings in CONSOLE_TRANSLATIONS and the framebuffer logo, neither of which
    # a stock sunfish kernel builds. oid_registry_data.c is stock.
    for f in drivers/tty/vt/consolemap_deftbl.c \
             drivers/video/logo/logo_linux_clut224.c \
             lib/oid_registry_data.c; do
        if [ -f ${B}/$f ]; then
            sed -i -e "s|${STAGING_KERNEL_DIR}/||g" ${B}/$f
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
# One thing this recipe deliberately does NOT do, and one that is now handled
# outside it:
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
# 2. The stock vendor modules are force-loaded, from the host side.
#
#    init.insmod.sunfish.cfg modprobes 48 names out of /vendor/lib/modules, 44
#    of which are actually shipped there, and the stock defconfig sets
#    CONFIG_MODVERSIONS, so their CRCs are checked. A kernel rebuilt with the
#    luneos.cfg delta moves those CRCs and every one of them is refused -
#    measured on hardware 22 Sep 2026, "disagrees about version of symbol
#    module_layout", 44 out of 44. luneos.cfg sets CONFIG_MODULE_FORCE_LOAD=y,
#    which makes the force possible; the vendor's own init.insmod.sh calls
#    plain `modprobe` with no --force, so the force has to come from the host
#    side, and it now does: the 45-vendor-modules pre-start hook in
#    android-system walks this same cfg, escalating plain -> --force-vermagic
#    -> --force per module. sunfish opts in through
#    vendor-modules.d/sunfish; that file carries the measured failure list.
#
#    What that recovers, all of it measured as broken without the modules:
#    wifi (wlan.ko - the firmware was already up, "icnss: WLAN FW is ready"),
#    audio (no ALSA card at all), the vibrator (drv2624, so no
#    /sys/class/leds/vibrator and a HAL that exited 124 times), vold's user-0
#    storage (incrementalfs), and - the one that is easy to miss - the two
#    DSPs. adsp_loader_dlkm.ko creates /sys/kernel/boot_adsp/boot, which the
#    cfg's own "enable|" lines write to; with no module there is no node, so
#    the ADSP and SLPI never start, fastrpc cannot open its channel
#    ("Transport endpoint is not connected"), adsprpcd restarts about four
#    times a second forever, CHRE never reaches chre_slpi and sensorfw reports
#    the device has no accelerometer.
#
#    Two pieces of good news measured off the trees rather than assumed:
#      - init.insmod.sh sets vendor.all.modules.ready / vendor.all.devices.ready
#        unconditionally at the end, *even when every modprobe failed*, so the
#        `wait_for_prop vendor.all.modules.ready 1` gate in init.hardware.rc
#        cannot wedge boot the way sargo's vendor.qcom.time.set did.
#      - The touchscreen really is built in (CONFIG_TOUCHSCREEN_FTS_S5), and it
#        works: the FTS driver talks to the panel on a kernel where not one
#        vendor module loaded.
#
#    Corrected here rather than quietly: this note used to say the audio stack
#    was built in too, reasoning from the nine symbols that are =m in
#    sunfish_defconfig. That is the wrong tree to reason from - what matters is
#    what Google shipped on the vendor partition, and Google built the whole
#    dlkm stack as modules (adsp_loader, apr, q6*, wcd*, swr*, *_macro, bolero,
#    machine, platform, stub, snd_event are all .ko there). There is no ALSA
#    card without them.
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
