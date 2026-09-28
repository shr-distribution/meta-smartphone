require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^fajita$"

DESCRIPTION = "Linux kernel for the OnePlus 6T (fajita, SDM845), the \
LineageOS 22.2 4.9 tree shared with the OnePlus 6 (enchilada)"

# Boot image geometry.
#
# VERIFIED against the shipping LineageOS image
# (lineage-22.2-20260922-nightly-fajita, boot.img published beside the zip), not
# just read out of BoardConfigCommon.mk: header v1, header_size 1648, 4096 byte
# pages, kernel 0x00008000, ramdisk 0x01000000, tags 0x00000100, recovery_dtbo
# 0/0, empty board name. sargo, surya and athena carry the same address set,
# which is why the kernel address looks unusually low (BOARD_KERNEL_BASE is
# 0x00000000 with the standard 0x8000 kernel offset).
#
# second_addr is the one address where the BoardConfig reading was wrong. AOSP's
# older mkbootimg defaulted --second_offset to 0x00f00000 and wrote base+offset
# unconditionally, so that is what this recipe had; the A15-era mkbootimg
# LineageOS builds with has dropped the second-stage image entirely and writes
# 0/0. The shipping image says 0x00000000, so that is what we write. It is inert
# either way - second_size is 0 and no bootloader reads the address of a section
# that is not there - but matching stock costs nothing and is the house rule.
ANDROID_BOOTIMG_HEADER_VERSION = "1"
ANDROID_BOOTIMG_PAGESIZE = "4096"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"
# No ANDROID_BOOTIMG_DTB_RAM_BASE: a v1 header has no dtb section at all. The
# device tree stays appended to Image.gz (KERNEL_IMAGETYPE = "Image.gz-dtb" in
# fajita.conf) and kernel_android.bbclass leaves it there for v1 - splitting it
# off, which it used to do, would drop it on the floor.

# Read back out of the shipping LineageOS 20260922 boot.img header rather than
# encoded from a guess - the same thing mindphone and surya do, and the reason
# this is no longer 0x1E000000 (15.0.0 with a deliberately empty patch level,
# which was the honest placeholder while no real value was available).
#
#   0x1E0001A9 = (15 << 25) | ((2026-2000) << 4) | 9  ->  15.0.0, 2026-09
#
# Matching stock keeps any bootloader check on this field happy. Bump it when the
# paired LineageOS build moves, along with SRCREV below.
ANDROID_BOOTIMG_OS_VERSION = "0x1E0001A9"

# The BoardConfig's BOARD_KERNEL_CMDLINE verbatim, plus three additions:
#
#   console=ttyMSM0,115200n8 / androidboot.console=ttyMSM0
#       fajita.dtsi retags qupv3_se9_2uart as the OEM geni console; LineageOS
#       ships no console= at all. Needs a jig to be useful. Same as sargo.
#   androidboot.selinux=permissive
#       bring-up. The container's own init still loads the vendor's policy.
#   printk.devkmsg=on
#       not cosmetic: the initramfs attaches a *dynamic* netconsole target and
#       replays the early printk buffer into it, which is how the host sees
#       anything from before the USB gadget came up. A dynamic target gets no
#       CON_PRINTBUFFER, so without this the replay is rejected and boot output
#       starts mid-story. athena and surya carry it for the same reason, and
#       there is no usable UART here to fall back on.
#
# Deliberately absent: datapart=/systempart=. The initramfs finds userdata and
# the Android images by partition label (mdev-partlabel.sh populates
# /dev/disk/by-partlabel), which is what lets one generic rootfs work; sargo
# hardcodes datapart= only because of its own history.
ANDROID_BOOTIMG_CMDLINE = "console=ttyMSM0,115200n8 androidboot.console=ttyMSM0 androidboot.configfs=true androidboot.hardware=qcom androidboot.usbcontroller=a600000.dwc3 ehci-hcd.park=3 firmware_class.path=/vendor/firmware_mnt/image loop.max_part=7 lpm_levels.sleep_disabled=1 msm_rtb.filter=0x237 service_locator.enable=1 swiotlb=2048 androidboot.selinux=permissive printk.devkmsg=on"

inherit kernel_android pkgconfig

# Optional initramfs debug shell, off by default.
#
# Built into the kernel command line rather than added to
# ANDROID_BOOTIMG_CMDLINE. On a Pixel that is the only way that works, because
# its bootloader drops arguments it does not recognise (see halium-kernel.inc);
# nobody has established whether the 6T's ABL is that strict, and the built-in
# route works either way.
#
#   MACHINE=fajita LUNEOS_ENABLE_ADB=1 bitbake linux-oneplus-fajita
#
# after adding LUNEOS_ENABLE_ADB to BB_ENV_PASSTHROUGH_ADDITIONS, or set it in
# local.conf.
LUNEOS_ENABLE_ADB ??= "0"

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/LineageOS/android_kernel_oneplus_sdm845.git;branch=lineage-22.2;protocol=https \
           file://0001-qcacld-drop-Werror-from-the-vendor-Kbuild.patch \
           file://luneos.cfg \
           file://module-signing/luneos-module-signing-key.pem \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
# CONFIRMED to be the exact revision the shipping build uses, not merely the
# branch head: build-manifest.xml published beside
# lineage-22.2-20260922-nightly-fajita-signed.zip pins
# LineageOS/android_kernel_oneplus_sdm845 at this SHA. So the rebuilt kernel is
# the same source as the one the phone runs, and meets the same /vendor blobs.
#
# The paired userspace revisions from that same manifest, for when this is bumped:
#   android_device_oneplus_fajita        63e45b838bb6707012f0eb8e39876c72552e3da2
#   android_device_oneplus_sdm845-common ed71e26a59ddca84832c6b0a9c2e2079198fcd2a
SRCREV = "228c16bfcd256a546e3fde375215f6072135588f"

LINUX_VERSION = "4.9"
KV = "4.9.337"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

DEPENDS += "dtc-native openssl-native"

# enchilada_defconfig is the config the LineageOS device tree selects
# (TARGET_KERNEL_CONFIG in device/oneplus/sdm845-common/BoardConfigCommon.mk) -
# one config for both SDM845 OnePlus phones, the per-board difference being the
# dtbo overlay rather than the kernel. luneos.cfg is the LuneOS/Halium delta on
# top of it; later lines win over earlier ones when kconfig reads the
# concatenation.
#
# LineageOS also appends vendor/debugfs.config, which turns DEBUG_FS and the
# page-owner tracking off for release builds. Deliberately not applied here:
# during bring-up /sys/kernel/debug is worth having, and nothing in LuneOS
# depends on it being absent.
do_configure:prepend() {
    cat ${S}/arch/arm64/configs/enchilada_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

do_configure:append() {
    kernel_conf_variable_fixup() {
        sed -i "/CONFIG_$1[ =]/d" ${B}/.config
        kernel_conf_variable $1 $2 ${B}/.config
    }

    # USB gadget functions that oldconfig turns from Y to M on these 4.9 msm
    # trees, which breaks the configfs gadget the initramfs and adb rely on.
    # Copied from sargo, the other 4.9 device here, where this was diagnosed.
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

    # Pin the module signing key rather than letting certs/ generate a fresh
    # one on every build - see the note below.
    sed -i '/CONFIG_MODULE_SIG_KEY/d' ${B}/.config
    echo 'CONFIG_MODULE_SIG_KEY="certs/luneos-module-signing-key.pem"' >> ${B}/.config

    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
    fi

    oe_runmake olddefconfig || oe_runmake oldnoconfig
}

# CONFIG_MODULE_SIG_FORCE=y is on in enchilada_defconfig, so the kernel loads
# only modules signed by the key it was built with. Nothing on this device
# needs that to be the *stock* key: LineageOS ships no prebuilt vendor modules
# for fajita at all (no .ko anywhere in device/oneplus/{fajita,sdm845-common},
# and wifi is CONFIG_QCA_CLD_WLAN=y, built in), so there are no vendor
# vermagic/CRCs a rebuild could break and no Tier A KMI discipline to keep.
#
# What the pinned key buys is out-of-tree module recipes (module.bbclass,
# e.g. wireguard-module) staying loadable across kernel rebuilds. With no
# pinned key, certs/ generates a new key per build and bakes its public half
# in, so a module signed against one build's STAGING_KERNEL_BUILDDIR stops
# loading the moment the kernel is rebuilt - "Required key not available" -
# even with no source change. certs/Makefile only regenerates when
# CONFIG_MODULE_SIG_KEY is exactly "certs/signing_key.pem", so naming a
# different file sidesteps that rule entirely. The full reasoning is in
# linux-google-sargo_git.bb.
#
# This is deliberately the same key as sargo's, byte for byte: it is a build
# key, not a secret, and two devices agreeing on it is the point. When a third
# device wants it, promote it to a shared location under meta-android rather
# than making a third copy.
do_compile:prepend() {
    mkdir -p ${B}/certs
    cp ${UNPACKDIR}/module-signing/luneos-module-signing-key.pem ${B}/certs/luneos-module-signing-key.pem
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
# Same treatment as surya, athena and mindphone.
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
# pinning the host C standard here is enough. Same fix as sargo, surya, athena
# and tissot - every pre-6.7 tree in this layer needs it.
BUILD_CFLAGS:append = " -std=gnu17"
