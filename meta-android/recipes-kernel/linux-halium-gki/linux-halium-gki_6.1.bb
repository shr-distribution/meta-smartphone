SUMMARY = "Android Common Kernel (GKI) android14-6.1, with the LuneOS config fragment"
DESCRIPTION = "\
One kernel per KMI, not per device: android14-6.1 serves every device whose \
vendor modules were built against that KMI - bluejay and panther today. \
\
This is the Tier A kernel of the GSI/GKI plan. The device keeps its own stock \
vendor modules (vendor_boot's dlkm ramdisk and vendor_dlkm); we only replace \
the GKI core. That only works while the exported-symbol CRCs stay exactly what \
those modules were built against, which is why this recipe does NOT use the \
OE cross toolchain: it builds with the same Clang the KMI was frozen with \
(r487747c / 17.0.2), taken from Google's own prebuilt. \
\
Verified 2026-09-12 on bluejay: built this way, with the LuneOS fragment \
applied, all 203 stock vendor modules from the bp4a factory image still load \
(0 CRC mismatches), and every symbol this build and Google's own Kleaf build \
both export has an identical CRC. Bazel is not required; plain kbuild is. \
Check it yourself after any change with the tools in the \
luneos-bootimg-bluejay repository: \
  kmi-crc-check.py <symvers> <dir of stock .ko>   -> expect 0 failures \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# The Clang the KMI was frozen with. An OE-built clang is a different major
# version (22 vs 17) and is NOT interchangeable here: it has never been
# validated against this KMI, and a silent CRC or CFI drift would show up as
# "vendor modules do not load" on the device, long after the build succeeded.
#
# 2.3 GB unpacked, fetched once per KMI and cached in DL_DIR. Provide it either
# way round:
#   GKI_CLANG_DIR  - path to an existing prebuilts/clang/host/linux-x86/clang-*
#                    directory (e.g. from a repo-synced kernel manifest)
#   GKI_CLANG_URI  - URI of a tarball of that directory, plus its checksums
GKI_CLANG_VERSION ?= "r487747c"
GKI_CLANG_DIR ?= ""
# pahole, for CONFIG_DEBUG_INFO_BTF. oe-core has no dwarves recipe, and using
# Google's own is better than a distro one anyway: it is the tool the KMI's BTF
# was generated with. Point this at prebuilts/kernel-build-tools/linux-x86 from
# the same kernel manifest as the Clang above.
GKI_BUILD_TOOLS_DIR ?= ""
GKI_CLANG_URI ?= ""

SRC_URI = "\
    git://android.googlesource.com/kernel/common;protocol=https;nobranch=1;name=kernel \
    ${@(d.getVar('GKI_CLANG_URI') + ';name=clang;subdir=clang') if d.getVar('GKI_CLANG_URI') else ''} \
    file://luneos_defconfig \
"
# nobranch=1: ACK release tags point at commits that are NOT reachable from the
# android14-6.1 branch tip (verified - `git branch -r --contains` is empty for
# this one, while `git tag --contains` lists android14-6.1-2026-06_r7/r8/r9),
# so a branch= pin cannot find the revision.
KMI_BRANCH ?= "android14-6.1"
# android14-6.1-2026-06_r7. Newer than the kernel/manifest pin on purpose: the
# manifest revision (6.1.124) predates the vendor modules in current factory
# images, and two vendor-hook modules (vh_mm, vh_sched) cannot resolve their
# tracepoints against it.
SRCREV_kernel = "87c0a9ca734b0d9adb40283aea035ada95a67a90"
SRCREV_FORMAT = "kernel"

PV = "6.1.172+git"
# No S assignment: oe-core sets S = "${UNPACKDIR}/${BP}" and the git
# fetcher unpacks to exactly that, and it now errors out if a recipe
# overrides it with the old ${WORKDIR}/git.
B = "${WORKDIR}/build"

# Host tools only. Nothing here compiles anything that runs on the target:
# the kernel proper is built by the pinned Clang, not by OE's cross toolchain.
DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native"

inherit deploy

COMPATIBLE_MACHINE = "^(bluejay|panther)$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
# Nothing is packaged: the boot image consumes the deployed Image directly.
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

python do_check_toolchain() {
    clang_dir = d.getVar("GKI_CLANG_DIR")
    if not clang_dir and not d.getVar("GKI_CLANG_URI"):
        bb.fatal("Neither GKI_CLANG_DIR nor GKI_CLANG_URI is set. This kernel "
                 "must be built with Google's Clang %s - see the comment in "
                 "%s." % (d.getVar("GKI_CLANG_VERSION"), d.getVar("FILE")))
    if clang_dir and not os.path.exists(os.path.join(clang_dir, "bin", "clang")):
        bb.fatal("GKI_CLANG_DIR %s has no bin/clang" % clang_dir)
    # CONFIG_DEBUG_INFO_BTF needs pahole, and the kernel only says so after it
    # has linked vmlinux - about 20 minutes in. Check it up front.
    tools = d.getVar("GKI_BUILD_TOOLS_DIR")
    if not tools or not os.path.exists(os.path.join(tools, "bin", "pahole")):
        bb.fatal("GKI_BUILD_TOOLS_DIR must point at a "
                 "prebuilts/kernel-build-tools/linux-x86 containing bin/pahole "
                 "(needed for CONFIG_DEBUG_INFO_BTF).")
}
addtask check_toolchain before do_configure

def gki_clang_bin(d):
    p = d.getVar("GKI_CLANG_DIR")
    if p:
        bins = [os.path.join(p, "bin")]
    else:
        bins = [os.path.join(d.getVar("UNPACKDIR"), "clang",
                             "clang-" + d.getVar("GKI_CLANG_VERSION"), "bin")]
    tools = d.getVar("GKI_BUILD_TOOLS_DIR")
    if tools:
        bins.append(os.path.join(tools, "bin"))
    return ":".join(bins)

# OE exports its own cross toolchain and flags into every task. None of it
# applies here - the kernel is built by the pinned Clang, for the target, by
# kbuild - and some of it is actively fatal: OE's CFLAGS carry
# -fcanon-prefix-map, which Clang 17 does not know, so vmlinux fails to link.
# kernel.bbclass unsets the same set for the same reason.
gki_clear_oe_env() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    unset CC CXX CPP LD AR NM STRIP OBJCOPY OBJDUMP RANLIB READELF
    unset HOSTCC HOSTCXX BUILD_CC BUILD_CXX MACHINE
}

KERNEL_MAKE = "make -C ${S} O=${B} LLVM=1 ARCH=arm64"

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    ${KERNEL_MAKE} gki_defconfig
    # merge_config.sh rewrites .config in place and warns about anything the
    # fragment asked for that did not survive - which is how a typo in the
    # fragment gets caught here rather than on the device.
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
        ${B}/.config ${UNPACKDIR}/luneos_defconfig
    ${KERNEL_MAKE} olddefconfig
}

do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    # resolve_btfids builds against elfutils from the native sysroot. It passes
    # HOSTCFLAGS through to libbpf as EXTRA_CFLAGS and then appends -Werror, so
    # a blanket -Wno-error is overridden and the specific warning has to be
    # named instead (libbpf 6.1 vs a newer elfutils' const-correctness).
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-I${STAGING_INCDIR_NATIVE} -Wno-incompatible-pointer-types-discards-qualifiers" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.lz4 modules
}

do_install[noexec] = "1"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/arch/arm64/boot/Image.lz4 ${DEPLOYDIR}/Image.lz4
    # Keep the symbol table next to the image: it is the input to the KMI check
    # and the only way to tell, without a device, whether a config change just
    # stopped the vendor's modules from loading.
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
}
addtask deploy after do_compile before do_build
