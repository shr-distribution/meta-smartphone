require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs. Already
# correct on sargo's stock defconfig, but the options here are forced rather
# than appended, so requiring it too is a harmless no-op that keeps every
# waydroid-capable device consistent instead of relying on each one's
# defconfig happening to already agree.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^sargo$"

DESCRIPTION = "Linux kernel for Google Pixel 3a device based on the \
sources from Droidian and LineageOS"

# lpm_levels.sleep_disabled=1 (a bring-up leftover) is deliberately absent: it
# turns off every SoC low-power mode, so all idle time was spent in shallow WFI
# - cpuidle C1-C3 usage was exactly 0 after hours of uptime. With LPM enabled
# the deep C-states engage within seconds and the device is stable.
# qpnp_smb2.debug_mask=0 silences the charger driver's PR_* debug logging,
# which the driver ships enabled (mask 31) and which printed POWER_PATH_STATUS
# to the journal about 20 times a second.
ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM0,115200n8 androidboot.console=ttyMSM0 printk.devkmsg=on msm_rtb.filter=0x237 ehci-hcd.park=3 service_locator.enable=1 firmware_class.path=/vendor/firmware datapart=/dev/mmcblk0p72 cgroup.memory=nokmem qpnp_smb2.debug_mask=0"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

# Android 11's bootloader will not take the v0 header abootimg produces. Stock
# and TWRP both ship header version 2 with 4096 byte pages and the device tree
# in its own section; a v0 image is refused with "Error boot prepare" before
# the kernel is ever reached. Addresses below are stock's.
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x01f00000"

inherit kernel_android pkgconfig

# Optional initramfs debug shell, off by default.
#
# The usual way in - an "enable_adb" boot image, as athena and surya ship - does
# not work on this device. A Pixel bootloader passes on only the command line
# arguments it already recognises and silently drops the rest, so nothing added
# to ANDROID_BOOTIMG_CMDLINE reaches /proc/cmdline; see the note in
# halium-kernel.inc, which was measured here with a "zz.marker=1" canary. init.sh
# greps /proc/cmdline for enable_adb, so the flag has to be built into the kernel.
#
# Build a debug image with:
#   MACHINE=sargo LUNEOS_ENABLE_ADB=1 bitbake linux-google-sargo
# after adding LUNEOS_ENABLE_ADB to BB_ENV_PASSTHROUGH_ADDITIONS, or set it in
# local.conf. sargo-staging/make-debug-boot.sh does both for you.
LUNEOS_ENABLE_ADB ??= "0"

do_configure:append() {
    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
    fi
}

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=sargo/${LINUX_VERSION}/lune;protocol=https"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRCREV = "eec812f3723e6cc5bcf410789c64e3b7f8966811"

LINUX_VERSION = "4.9.124"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native python3-dtschema-wrapper-native openssl-native"

do_configure:prepend() {
    cp -v -f ${S}/arch/arm64/configs/lineageos_bonito_defconfig ${WORKDIR}/defconfig
}

do_configure:append() {
  kernel_conf_variable_fixup() {
      sed -i "/CONFIG_$1[ =]/d" ${B}/.config
      kernel_conf_variable $1 $2 ${B}/.config
  }

# fixup some options which get changes from Y to M in oldconfig :/
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
  oe_runmake oldnoconfig
}

do_install:append () {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    rm -rf ${D}/usr/src/usr
}

# scripts/unifdef.c declares "static bool constexpr;" and assigns to it.
# C23 made constexpr a keyword, and this host GCC (15.x on Ubuntu 26.04) defaults
# to -std=gnu23, so building the kernel host tools fails:
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#   scripts/unifdef.c:880:27: error: expected identifier or '(' before '=' token
#
# Upstream renamed the variable in 6.7; this kernel predates that. Only the host
# tools are affected - the kernel itself is built with OE's cross toolchain -
# and kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host
# C standard here is enough.
#
# Same fix as linux-xiaomi-tissot-halium: both are 4.9 trees from
# shr-distribution/linux, so both hit this the moment the host compiler moved on.
BUILD_CFLAGS:append = " -std=gnu17"

# Pin the module signing key rather than letting the kernel's own certs/
# machinery generate a fresh one on every build.
#
# CONFIG_MODULE_SIG_FORCE=y is on for this device. Out-of-tree kernel modules
# built with module.bbclass are otherwise a well-trodden, low-risk path here -
# this is the one piece that trips that up. With no pinned key, every
# from-scratch kernel build generates a brand new self-signed key and bakes
# its public half in as the kernel's own trusted key - so a module built and
# signed against *this* build's STAGING_KERNEL_BUILDDIR (shared across
# recipes that depend on virtual/kernel, e.g. wireguard-module) will only
# ever load on a kernel that was built in that exact same session. Rebuild
# the kernel later, even with no source change at all, and it's "Required
# key not available" the moment you try to load any out-of-tree module
# again.
#
# certs/Makefile only auto-(re)generates the key when CONFIG_MODULE_SIG_KEY
# is exactly the literal string "certs/signing_key.pem" - that path's rule
# depends on certs/x509.genkey, which is rewritten with a fresh mtime on
# every single build, so a key merely *copied* to that exact path before
# do_compile still gets regenerated (confirmed: still a fresh key after
# adding a do_compile:prepend copy there). Pointing CONFIG_MODULE_SIG_KEY at
# a different filename entirely sidesteps that rule altogether - kbuild then
# just uses whatever is there, no regeneration logic in play at all.
#
# Committing one fixed key here makes both sides of that relationship
# (kernel's trusted key, module's signature) deterministic across rebuilds -
# necessary for this to actually be reusable for other pre-5.6 devices
# (tissot, ...) built later, not just a one-off fix for tonight's kernel
# build.
SRC_URI += "file://module-signing/luneos-module-signing-key.pem"

do_configure:append() {
    sed -i '/CONFIG_MODULE_SIG_KEY/d' ${B}/.config
    echo 'CONFIG_MODULE_SIG_KEY="certs/luneos-module-signing-key.pem"' >> ${B}/.config
}

do_compile:prepend() {
    mkdir -p ${B}/certs
    cp ${UNPACKDIR}/module-signing/luneos-module-signing-key.pem ${B}/certs/luneos-module-signing-key.pem
}
