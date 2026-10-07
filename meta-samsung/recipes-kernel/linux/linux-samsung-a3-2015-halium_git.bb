require recipes-kernel/linux/linux.inc
# Options every Halium target needs; see the file for what and why.
require recipes-kernel/linux/halium-kernel.inc
# The build fixes of the 3.4 Halium kernels apply to this 3.10 tree as well: the C standard and no-PIE
# flags for the target compiler, -std=gnu17 for the host tools (scripts/unifdef.c names a variable
# constexpr, a C23 keyword, and "make headers_install" fails without it) and the mach-types banner
# rewrite for the buildpaths check. Its renameat2 patch is only for machines that set
# HALIUM_LEGACY_GSI, which this one does not: this kernel has renameat2 already.
require recipes-kernel/linux/halium-kernel-3.4.inc

SECTION = "kernel"

# Mark archs/machines that this kernel supports
COMPATIBLE_MACHINE = "^a3-2015-halium$"

DESCRIPTION = "Linux kernel for the Samsung Galaxy A3 (2015) (SM-A300F/FU/H/..., a3lte, MSM8916) \
on Halium, built from vlw's LineageOS 18.1 branch"

#-----------------------------------------------------------------------------
# Source
#-----------------------------------------------------------------------------
# github.com/vlw/android_kernel_samsung_msm8916-caf, branch lineage-18.1: Samsung's MSM8916 kernel on
# a full CAF 3.10 base (LA.BR.1.2.9), maintained by vlw for the LineageOS 16.0 to 19.1 builds of the
# A3 (2015), the kernel those builds boot. It is a 32-bit ARM kernel (arch/arm, zImage), as the
# LineageOS a3lte build is 32-bit (FORCE_32_BIT in its BoardConfig.mk), and it already has binder
# with hwbinder/vndbinder, ashmem, seccomp, getrandom, memfd_create and renameat2 (__NR_renameat2 at
# 382), so Android 16's bionic needs nothing backported.
#
# Built from a3-2015/3.10/lineage-18.1 in shr-distribution/linux: vlw's lineage-18.1 head of
# 22 Nov 2025 (64f7a77a0f7c4c73b1c8b97df96fabccb552279b) with LuneOS' patches committed on top, as
# for sm-t520/3.4/lineage-18.1. The patches live there as commits, not as .patch files here.
SRCREV = "e18731f1a6de67fddb7bfa3b1f4986b5a9010896"

SRC_URI = "git://github.com/shr-distribution/linux.git;branch=a3-2015/3.10/lineage-18.1;protocol=https \
           file://luneos.cfg \
           ${A3_QCDT_URL};name=qcdt;downloadfilename=a3-2015-dtFU-${A3_QCDT_REV}.img;subdir=qcdt \
           "

LINUX_VERSION = "3.10.108"
PV = "${LINUX_VERSION}+git"
# for bumping PR bump MACHINE_KERNEL_PR in the machine config
inherit machine_kernel_pr

#-----------------------------------------------------------------------------
# Boot image
#-----------------------------------------------------------------------------
# From msm8916-common's BoardConfigCommon.mk (lineage-18.1): base 0x80000000, page size 2048,
# ramdisk offset 0x02000000, tags offset 0x01E00000, and the usual kernel (0x8000) and second
# (0x00f00000) offsets; header version 0. The command line is the LineageOS one. The boot partition
# is 13 MiB (BOARD_BOOTIMAGE_PARTITION_SIZE in a3lte's BoardConfig.mk).
ANDROID_BOOTIMG_CMDLINE = "console=null androidboot.hardware=qcom user_debug=23 msm_rtb.filter=0x3F ehci-hcd.park=3 androidboot.bootdevice=7824900.sdhci androidboot.selinux=permissive loop.max_part=7"
ANDROID_BOOTIMG_KERNEL_RAM_BASE = "0x80008000"
ANDROID_BOOTIMG_RAMDISK_RAM_BASE = "0x82000000"
ANDROID_BOOTIMG_SECOND_RAM_BASE = "0x80f00000"
ANDROID_BOOTIMG_TAGS_RAM_BASE = "0x81e00000"

inherit kernel_android

# DEBUG (bring-up): a kernel console log that survives a warm reset, for lk2nd to read back with
# "fastboot oem ramoops console". lk2nd keeps its ramoops region at the end of its scratch area
# (0x90000000 + 256 MiB - 512 KiB on this phone, "fastboot oem ramoops regions") and, for a
# downstream kernel, does not reserve it in the device tree, so the branch's "pstore/ram: reserve the
# ramoops region from the command line" adds ramoops_reserve= to keep the page allocator off it (with
# the initcall table of "init: record the initcalls started in lk2nd's scratch area" below it,
# 0x9ff00000). Layout as lk2nd reads it: 256 KiB of 8 KiB dump records, then a
# 256 KiB console. ignore_loglevel sends every message to the pstore console. In CMDLINE (the
# kernel's own CONFIG_CMDLINE) rather than the boot image header, so it does not depend on what
# the boot loader passes on.
A3_RAMOOPS_BASE = "0x9ff80000"
CMDLINE = "ramoops_reserve=0x100000@0x9ff00000 ignore_loglevel"

# kernel.bbclass sets S = "${STAGING_KERNEL_DIR}", and do_symlink_kernsrc only
# moves the unpacked tree there when the recipe points S somewhere else.
S = "${UNPACKDIR}/${BP}"

FILESEXTRAPATHS:prepend := "${THISDIR}/linux-samsung-a3-2015-halium:"

# lineageos_a3_defconfig is the config the LineageOS 18.1 build uses. luneos.cfg is the
# LuneOS/Halium delta on top: later lines win over earlier ones when kconfig reads the concatenation.
do_configure:prepend() {
    cat ${S}/arch/arm/configs/lineageos_a3_defconfig \
        ${UNPACKDIR}/luneos.cfg > ${WORKDIR}/defconfig
}

do_install:append() {
    # make headers_install leaves kbuild's ..install.cmd bookkeeping behind, and
    # linux.inc ships everything under ${exec_prefix}/src/linux* as kernel-headers.
    # Those files record absolute command lines, which wrynose rejects as
    # "contains reference to TMPDIR [buildpaths]".
    find ${D}${exec_prefix}/src -name '..install.cmd' -delete 2>/dev/null || true
    # This 3.10 tree's headers_install also writes the staging ION uapi header under
    # /usr/src/usr, which no package ships; athene's 3.10 kernel recipe drops it the same way.
    rm -rf ${D}${exec_prefix}/src/usr
}

#-----------------------------------------------------------------------------
# Device tree: a Qualcomm QCDT image in the boot image
#-----------------------------------------------------------------------------
# The bootloader takes the device tree from a QCDT table (magic "QCDT", version 2) placed after the
# kernel, ramdisk and second stage of a version 0 boot image, with its size in the header word that
# later versions use for header_version (offset 40). That is what LineageOS' mkbootimg writes for
# BOARD_KERNEL_SEPARATED_DT; abootimg, which kernel_android.bbclass uses for version 0, has no such
# section, so a3_qcdt_bootimg adds it to the deployed image afterwards.
#
# The table is the SM-A300FU's own, dtFU.img from vlw/proprietary_vendor_samsung: one device tree
# registered for MSM8916 platform ids 206, 248, 249 and 250 (variant 0xce08ff01). It is the one the
# LineageOS build puts into the boot image on an FU (a3lte/dtimages/kernel_make.sh, run by its
# installer), in place of the table it builds from this kernel's own device trees. The other models
# need their own table (dtH.img for the SM-A300H).
A3_QCDT_REV = "eeb50171bfd5992d8965cff5df8fff637c2b55df"
A3_QCDT_URL = "https://raw.githubusercontent.com/vlw/proprietary_vendor_samsung/${A3_QCDT_REV}/a3lte/dtimages/dtFU.img"
SRC_URI[qcdt.sha256sum] = "fc862a966d586cb651f62e78e9c167411b6153004afc6a3e9f9b88e0530d9ee4"

do_deploy[postfuncs] += "a3_qcdt_bootimg"

python a3_qcdt_bootimg() {
    import hashlib, os, struct

    qcdt = open(os.path.join(d.getVar("UNPACKDIR"), "qcdt",
                             "a3-2015-dtFU-%s.img" % d.getVar("A3_QCDT_REV")), "rb").read()
    if qcdt[:4] != b"QCDT":
        bb.fatal("dtFU.img is not a QCDT table")

    deploydir = d.getVar("DEPLOYDIR")
    for t in d.getVar("KERNEL_IMAGETYPES").split():
        path = os.path.join(deploydir, "%s-%s.fastboot" % (t, d.getVar("KERNEL_IMAGE_NAME")))
        img = bytearray(open(path, "rb").read())
        if img[:8] != b"ANDROID!":
            bb.fatal("%s is not an Android boot image" % path)
        ksize, _, rsize, _, ssize, _, _, page, dtsize = struct.unpack_from("<9I", img, 8)
        if dtsize:
            bb.fatal("%s already has a header version or dt size (%d) at offset 40" % (path, dtsize))
        pages = lambda n: (n + page - 1) // page * page
        end = page + pages(ksize) + pages(rsize) + pages(ssize)
        sections = [bytes(img[page:page + ksize]),
                    bytes(img[page + pages(ksize):page + pages(ksize) + rsize]),
                    bytes(img[page + pages(ksize) + pages(rsize):page + pages(ksize) + pages(rsize) + ssize]),
                    qcdt]
        # The image id is the sha1 over each section and its size, the dt included, as
        # LineageOS' mkbootimg computes it.
        sha = hashlib.sha1()
        for s in sections:
            sha.update(s)
            sha.update(struct.pack("<I", len(s)))
        struct.pack_into("<I", img, 40, len(qcdt))
        img[576:608] = sha.digest() + b"\0" * 12
        out = bytes(img[:end]) + qcdt + b"\0" * (-len(qcdt) % page)
        # The LineageOS build appends this marker to every boot image; without it the bootloader
        # warns that the kernel is not SEAndroid enforcing.
        out += b"SEANDROIDENFORCE"
        limit = 13631488
        if len(out) > limit:
            bb.fatal("%s is %d bytes, the boot partition holds %d" % (path, len(out), limit))
        with open(path, "wb") as f:
            f.write(out)
        bb.note("Added the %d byte QCDT table to %s (%d bytes)" % (len(qcdt), path, len(out)))
}
