require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# The build fixes of the 3.4 Halium kernels apply to this 3.10 tree as well: the C standard and no-PIE flags
# for the target compiler, -std=gnu17 for the host tools (scripts/unifdef.c names a variable constexpr, a C23
# keyword, and "make headers_install" fails without it) and the mach-types banner rewrite for the buildpaths
# check. Its renameat2 patch is only for machines that set HALIUM_LEGACY_GSI, which this one does not: this
# tree's table stops at finit_module, so the branch has its own version of that commit.
require recipes-kernel/linux/halium-kernel-3.4.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^goyavewifi$"

DESCRIPTION = "Linux kernel for the Samsung Galaxy Tab 3 Lite 7.0 / Tab E Lite Wi-Fi (SM-T113, goyavewifi, \
Spreadtrum SC7730S) on Halium, built from the community sc8830 hybris-14.1 tree"

#-----------------------------------------------------------------------------
# Source
#-----------------------------------------------------------------------------
# github.com/samsung-sc8830/android_kernel_samsung_sc8830, branch hybris-14.1 (f24b6282, 7 Dec 2022):
# Spreadtrum sc8830 on Linux 3.10.103, the newest 3.10 tree that exists for this SoC family and the only one
# written for libhybris. The stock kernel is 3.10.17 and the LineageOS 15.1 build of the tablet runs 3.10.89.
# It carries the goyavewifi device trees (rev02 to rev04) and board files, but no goyavewifi defconfig and
# not everything the tablet needs, so:
#
#   lineage_goyavewifi_defconfig  from github.com/mertsan2007/android_kernel_samsung_sc8830, lineage-15.1,
#                                 c32a0cc6d316: the config that tree builds the tablet with (this directory)
#   luneos.cfg                    the LuneOS/Halium delta (this directory)
#
# Built from goyavewifi/3.10/hybris-14.1 in shr-distribution/linux: that head with LuneOS' nine commits on top,
# as for a3-2015/3.10/lineage-18.1 and sm-t520/3.4/lineage-18.1. The commits are not .patch files here. They are:
# compiler-gcc15.h; ARM 8933/1 (upstream 790756c7e022); put_user() keeps its value in r2; MemAvailable;
# the goyavewifi board header; the Mali Kconfig loop; COMMAND_LINE_SIZE 2048; renameat2 (flags 0); and the
# multi-device binder of the LineageOS 15.1 tree. goyavewifi-STATUS.md says what each one fixes.
SRCREV = "732e698ac7dff55cf08626f37f14066aaafb34b2"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=goyavewifi/3.10/hybris-14.1;protocol=https \
           file://lineage_goyavewifi_defconfig \
           file://luneos.cfg \
           file://mksprdboot.py \
           "

LINUX_VERSION = "3.10.103"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

#-----------------------------------------------------------------------------
# Boot image
#-----------------------------------------------------------------------------
# Read off the stock boot.img (T113XXS0AQC2) and the LineageOS 15.1 one: header version 0, 2048 byte pages,
# kernel 0x8000, ramdisk 0x01000000, second 0x00f00000, tags 0x100. The stock command line is just the
# console; androidboot.hardware=sc8830 is in the kernel's own CONFIG_CMDLINE. The boot partition, named
# KERNEL, is 16 MiB (BOARD_BOOTIMAGE_PARTITION_SIZE in the LineageOS device tree).
ANDROID_BOOTIMG_CMDLINE = "console=ttyS1,115200n8 androidboot.selinux=permissive"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x00008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x01000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x00f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x00000100"

inherit kernel_android

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

# The kernel's own command line. linux.inc replaces CONFIG_CMDLINE with CMDLINE (" loglevel=3" by default, which it adds itself), which
# throws away the androidboot.hardware=sc8830 of lineage_goyavewifi_defconfig. That argument matters: the Android
# container's init takes ro.hardware from it, and picks /vendor/etc/init/hw/init.sc8830.rc and ueventd.sc8830.rc
# by that name. The LineageOS boot image carries nothing else of the kind, the config is the only place.
# halium-kernel.inc then appends the systemd cgroup switches (and enable_adb for a debug build).
CMDLINE = "androidboot.hardware=sc8830"

# Optional initramfs debug shell, off by default. init.sh greps /proc/cmdline for enable_adb and then stops in
# its adbd shell instead of continuing the boot. The flag is put into the kernel's own CONFIG_CMDLINE, which
# CONFIG_CMDLINE_EXTEND appends to whatever the bootloader passes: Samsung's sboot builds its own command line
# and it has not been measured here whether it keeps arguments of the boot image header (a Pixel bootloader
# drops them, see linux-google-sargo). Build the debug image with:
#
#   CONF=$(mktemp --suffix=.conf); echo 'LUNEOS_ENABLE_ADB = "1"' > $CONF
#   MACHINE=goyavewifi bitbake -R $CONF linux-samsung-goyavewifi
#
# (-R rather than an environment variable: setup-env pins BB_ENV_PASSTHROUGH_ADDITIONS.) The image is
# tmp/deploy/images/goyavewifi/zImage-goyavewifi.fastboot; rebuild without the switch afterwards to put the
# normal one back. goyavewifi-staging/make-debug-boot.sh in the test kit does all of that.
LUNEOS_ENABLE_ADB ??= "0"

do_configure:append() {
    if [ "${LUNEOS_ENABLE_ADB}" = "1" ]; then
        halium_kernel_add_cmdline "enable_adb"
    fi
}

# lineage_goyavewifi_defconfig is the config the LineageOS 15.1 build uses. luneos.cfg is the LuneOS/Halium
# delta on top: later lines win over earlier ones when kconfig reads the concatenation.
do_configure:prepend() {
    cat ${UNPACKDIR}/lineage_goyavewifi_defconfig \
        ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

# This tree's top-level Makefile assigns CROSS_COMPILE with "=" ("CROSS_COMPILE = $(CONFIG_CROSS_COMPILE...)"),
# which overrides the CROSS_COMPILE that kernel.bbclass exports in the environment. CC, LD, OBJCOPY and STRIP
# are passed on the command line and so still reach the cross tools, but nm, ar, as and objdump quietly become
# the host's, and the host nm lists the ARM "$d" mapping symbols of the decompressor as local .bss symbols:
#
#   following symbols must have non local/private scope:
#   $d
#
# (arch/arm/boot/compressed/Makefile, check_for_bad_syms). A command-line assignment wins over the Makefile's.
EXTRA_OEMAKE:append = " CROSS_COMPILE=${TARGET_PREFIX}"

# The host tools need one more flag than halium-kernel-3.4.inc gives them. This tree's scripts/dtc defines
# yylloc in both dtc-lexer.lex.o and dtc-parser.tab.o, which GCC 10 and later (-fno-common by default) refuse
# to link:
#
#   ld: scripts/dtc/dtc-parser.tab.o: multiple definition of `yylloc'; scripts/dtc/dtc-lexer.lex.o: first defined here
#
# The A3 (2015) tree has a newer dtc and does not hit it. kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}".
BUILD_CFLAGS:append = " -fcommon"

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true

    # The console font table is generated at build time and stamps the absolute path of its input into a
    # comment ("conmakehash <TMPDIR>/.../drivers/tty/vt/cp437.uni > [this file]"), which ships in the -src
    # package and trips the [buildpaths] check. In do_install, like halium-kernel-3.4.inc's mach-types.h
    # banner, so that this does not rebuild the kernel. The comment is all that changes.
    if [ -f ${B}/drivers/tty/vt/consolemap_deftbl.c ]; then
        sed -i 's|conmakehash .*/drivers/tty/vt/|conmakehash drivers/tty/vt/|' ${B}/drivers/tty/vt/consolemap_deftbl.c
    fi
}

#-----------------------------------------------------------------------------
# Device tree: a Spreadtrum "SPRD" table in the boot image
#-----------------------------------------------------------------------------
# The bootloader takes the device trees from a table placed after the kernel and ramdisk of a version 0 boot
# image, with its size in the header word that later versions use for header_version (offset 40):
#
#   "SPRD" u32 version=1 u32 count, count x { chip 0x227e, board revision, 0x20000, offset, size }, u32 0,
#   then the dtbs, 2048 aligned in 0xd800 byte slots.
#
# The stock image has revisions 2, 3 and 4; LineageOS' has only 4. mksprdboot.py (this directory) writes the
# table, and rebuilds the stock boot.img byte for byte from its own parts (its "selftest"), which is how the
# layout above was checked. abootimg, which kernel_android.bbclass uses for version 0, has no such section, so
# sprd_bootimg_deploy rebuilds the deployed image afterwards with the kernel, the initramfs and the three
# dtbs. KERNEL_DEVICETREE stays empty on purpose: the class would otherwise take the three names for one path.
do_compile:append() {
    oe_runmake dtbs CC="${KERNEL_CC}" LD="${KERNEL_LD}"
}

do_deploy[postfuncs] += "sprd_bootimg_deploy"

python sprd_bootimg_deploy() {
    import os, sys
    sys.path.insert(0, d.getVar("UNPACKDIR"))
    import mksprdboot as m

    b = d.getVar("B")
    dts = os.path.join(b, d.getVar("KERNEL_OUTPUT_DIR"), "dts")
    dtbs = {}
    for rev in (2, 3, 4):
        with open(os.path.join(dts, "sprd-scx35_goyavewifi_rev%02d.dtb" % rev), "rb") as f:
            dtbs[rev] = f.read()
    initramfs = os.path.join(d.getVar("DEPLOY_DIR_IMAGE"), d.getVar("INITRAMFS_NAME"))
    with open(os.path.join(b, d.getVar("KERNEL_OUTPUT")), "rb") as f:
        kernel = f.read()
    with open(initramfs, "rb") as f:
        ramdisk = f.read()
    img = m.build_boot(kernel, ramdisk, m.build_dt(dtbs),
                       d.getVar("ANDROID_BOOTIMG_CMDLINE").encode())

    limit = 16777216
    if len(img) > limit:
        bb.fatal("The boot image is %d bytes, the KERNEL partition holds %d" % (len(img), limit))

    deploydir = d.getVar("DEPLOYDIR")
    name = d.getVar("KERNEL_IMAGE_NAME")
    for t in d.getVar("KERNEL_IMAGETYPES").split():
        path = os.path.join(deploydir, "%s-%s.fastboot" % (t, name))
        with open(path, "wb") as f:
            f.write(img)
        bb.note("Wrote %s with a SPRD device-tree table (%d bytes)" % (path, len(img)))
}
