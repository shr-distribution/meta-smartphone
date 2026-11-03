require recipes-kernel/linux/linux.inc
# Binder nodes, veth, overlayfs and the rest of what Waydroid needs from a
# kernel this old. See the include for why each one is there.
require recipes-kernel/linux/waydroid-kernel.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "mindphone"

DESCRIPTION = "Linux kernel for the GreenTouch (Mindphone) device based on the official \
source from GreenTouch"

# Everything below mirrors the stock boot.img extracted from the vendor OTA
# (see gsigki/mindphone/mindphone-notes.md): header v2, 2048 byte pages,
# 32-bit zImage. bootopt is how MTK lk knows this is a 64-bit SoC running a
# 32-bit kernel and userland — do not touch it.
ANDROID_BOOTIMG_CMDLINE = "bootopt=64S3,32S1,32S1 buildvariant=userdebug"
ANDROID_BOOTIMG_HEADER_VERSION = "2"
ANDROID_BOOTIMG_PAGESIZE = "2048"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x40008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x45000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00000000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x44000000"
ANDROID_BOOTIMG_DTB_RAM_BASE = "0x44000000"
# 11.0.0, security patch 2022-03, same as stock so lk's rollback check stays happy
ANDROID_BOOTIMG_OS_VERSION = "0x16000163"
# The dtb section must be the stock dt_table blob (magic 0xd7b7ab1e), not a
# bare FDT: lk only parses the table format. Extracted from the stock boot.img.
ANDROID_BOOTIMG_DTB = "${UNPACKDIR}/dt-table.img"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=dnim/4.14.186;protocol=https \
           file://luneos.cfg \
           file://dt-table.img \
           "

# The device runs a 32-bit kernel (stock is an armv7 zImage; bootopt says
# 32S1 kernel/userland), so the base is the arm defconfig for the k39tv1_bsp_1g
# project the stock IKCONFIG names — not the arm64 k39tv1_64 one. The LuneOS
# additions live in luneos.cfg in this layer; later lines win over earlier
# ones when kconfig reads the concatenation.
do_configure:prepend() {
    cat ${S}/arch/arm/configs/k39tv1_bsp_1g_defconfig ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# shr-distribution/linux dnim/4.14.186:
# "fork: CLONE_PIDFD: do not require a zeroed *parent_tidptr" - without it the
# Sep 25 waitid(P_PIDFD) backport makes Qt's QProcess::startDetached() fail with
# EINVAL, so the compositor never reports ready and restarts every 90 s.
# "video: pwm10: clear stray CON_0 bits on mt6739" - the backlight turned full
# white at screen-off, with brightness inverted, until the next reboot.
# Revert "video: mt6739: resume display in mtkfb_open when slept" - it left the
# panel asleep behind a lit backlight after every screen-on.
# "ASoC: mt6357: start the left headphone gain at 0 dB too" - headphones ran at
# +8 dB until something set a gain, which on Halium nothing does.
# "mtk_power_misc: ignore the UI SOC 1% shutdown when the gauge disagrees" -
# fuelgauged restored a UI SOC of 0 from the RTC and the kernel powered the
# phone off at 1% with the battery at 4.0 V and the gauge at 72%.
# "mediatek: connectivity: import fmradio driver" and "... build fmradio
# in-tree as a module" - the vendor fmradio_drv.ko was built without
# CGROUP_WRITEBACK, read inode->i_cdev 16 bytes early and oopsed the phone on
# the first open of /dev/fm. MediaTek's GPL source (Motorola android-11
# release) now builds as kernel-module-fmradio-drv, which mtk-load-modules.sh
# prefers over the vendor copy.
SRCREV = "b4562028af68a571567ede77e545a466ccb5e274"

KV = "4.14.186"
PV = "${KV}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
}

do_compile:append() {
    # arch/arm/tools/gen-mach-types stamps awk's FILENAME - the path kbuild
    # handed it - into the banner of the header it generates, and with an
    # out-of-tree build that is an absolute path under TMPDIR. linux.inc ships
    # the build tree as -src, so the header goes out with it and wrynose
    # rejects the package: "contains reference to TMPDIR [buildpaths]".
    #
    # Rewrite the banner to name the file the way an in-tree build would.
    # It is a comment; the machine number defines below it are what anything
    # actually reads, and they do not change.
    if [ -f ${B}/arch/arm/include/generated/asm/mach-types.h ]; then
        sed -i 's|generated from .*/arch/arm/tools/mach-types!|generated from arch/arm/tools/mach-types!|' \
            ${B}/arch/arm/include/generated/asm/mach-types.h
    fi
}
