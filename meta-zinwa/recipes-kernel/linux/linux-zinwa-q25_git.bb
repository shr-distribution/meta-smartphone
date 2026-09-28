SUMMARY = "Zinwa Q25 kernel - MediaTek mgk 5.10.198 (android12-5.10 KMI gen 9) + the LuneOS fragment"
DESCRIPTION = "\
The Q25's own kernel, built from the device tree LineageOS publishes, not from \
the Android Common Kernel. \
\
That is a deliberate departure from bluejay and panther, and it was forced by \
measurement rather than chosen. The Q25 looks like a textbook Tier A device - \
GKI boot image, android12-5.10 KMI generation 9, stock vendor_boot and \
vendor_dlkm - so the port first built ACK 5.10.250 with gki_defconfig plus the \
Halium fork's fragments, exactly as the Tier A method says. Checked against the \
344 stock vendor modules pulled out of the stock firmware, that kernel scored \
**344 of 344 would fail to load**, with `module_layout` itself mismatching. \
\
The reason is visible in the stock kernel's own IKCONFIG: it has \
CONFIG_ARCH_MEDIATEK=y and about 1,200 options that gki_defconfig does not, \
because MediaTek does not ship pure GKI here. Their kernel is 'mgk' - \
gki_defconfig merged with mgk_64_k510_defconfig (646 lines, 280 CONFIG_MTK*), \
then entry_level.config, then the board's q20_v12_factory.config - on a source \
tree that carries drivers/misc/mediatek and drivers/gpu/mediatek out of tree. \
The stock vendor modules' CRCs were computed against that, so no ACK build of \
any vintage can match them. \
\
So the KMI discipline is unchanged - the device keeps its own stock vendor \
modules and we replace only the kernel - but the baseline we have to reproduce \
is MediaTek's, not Google's. Same source (5.10.198, which is exactly the stock \
kernel's version), same four-fragment config chain, same Clang the KMI was \
frozen with, plus the LuneOS delta on top. \
\
Gate, every time, before flashing: \
  ./check-kmi.sh   (in /home/herrie/webos/LuneOS/zinwa)  -> 0 would fail \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Same Clang the stock kernel was built with - its version string says
# "Android (7284624, based on r416183b) clang version 12.0.5" - and the same one
# TARGET_KERNEL_CLANG_VERSION names in the device's BoardConfig.mk. Set
# GKI_CLANG_DIR:q25 in local.conf; fetch-gki-toolchain.sh populates it.
GKI_CLANG_VERSION ?= "r416183b"
GKI_CLANG_DIR ?= ""
GKI_CLANG_URI ?= ""

SRC_URI = "\
    git://github.com/LineageOS/android_kernel_xelex_mt6789.git;protocol=https;branch=lineage-23.2;name=kernel \
    ${@(d.getVar('GKI_CLANG_URI') + ';name=clang;subdir=clang') if d.getVar('GKI_CLANG_URI') else ''} \
    file://vermagic.cfg;subdir=frag \
    file://0001-bbqX0kbd-Q20-symbol-layers.patch \
    file://0002-bbqX0kbd-sym-tap-emoji-key.patch \
    file://luneos.cfg;subdir=frag \
"
SRCREV_kernel = "2a873a3511ee0eeead1442e60145093192a4535d"

# The one patch in this directory, and it should not stay here.
#
# 0001-bbqX0kbd-Q20-symbol-layers.patch makes the physical keyboard able to type
# punctuation at all under LuneOS - see its own commit message, and the keyboard
# section of ~/webos/LuneOS/zinwa/zinwa-q25-notes.md for how the gap was found.
#
# Every other LuneOS kernel carries its fixes as commits on an shr-distribution
# branch with a SRCREV bump, never as .patch files next to the recipe (see
# linux-minimal-mp01_git.bb, which says so). This one is a patch only because
# the Q25 still fetches LineageOS's tree directly. When a
# shr-distribution/linux branch exists for this device - q25/5.10.198/lune, by
# the MP01's naming - this becomes a commit there and this line goes away.
SRCREV_FORMAT = "kernel"

PV = "5.10.198+git"
B = "${WORKDIR}/build"

DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native"

inherit deploy

COMPATIBLE_MACHINE = "^(q25)$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

# The device's own config chain, verbatim from BoardConfig.mk:
#   TARGET_KERNEL_CONFIG := gki_defconfig mgk.config entry_level.config \
#                           q20_v12_factory.config
# mgk.config and entry_level.config are git symlinks inside the tree
# (-> mgk_64_k510_defconfig and -> ../../../kernel/configs/entry_level.config);
# kbuild's %.config rule resolves them, so they are named here as-is.
#
# LUNEOS_KERNEL_FRAGMENT = "0" builds the pure stock baseline instead, which is
# how you establish that the tree and toolchain reproduce the stock CRCs before
# blaming your own delta for a KMI failure. Keep that habit: baseline first,
# then the delta.
Q25_KERNEL_CONFIGS ?= "gki_defconfig mgk.config entry_level.config q20_v12_factory.config"
LUNEOS_KERNEL_FRAGMENT ?= "1"
# Trade the stock full LTO for ThinLTO. The device config sets
# CONFIG_LTO_CLANG_FULL, whose final "LTO vmlinux.o" link is single-threaded -
# measured at ~20 minutes of one core here, which is most of the build. ThinLTO
# parallelises it. KMI-neutral by construction: CRCs come from genksyms on
# preprocessed source, not from codegen, and the MP01 verified 0/345 both ways
# on this same SoC. check-kmi.sh is still the gate per build. Set to "0" for a
# shipped image if you would rather match the vendor's codegen exactly.
KERNEL_LTO_THIN ?= "0"

python do_check_toolchain() {
    clang_dir = d.getVar("GKI_CLANG_DIR")
    if not clang_dir and not d.getVar("GKI_CLANG_URI"):
        bb.fatal("Neither GKI_CLANG_DIR nor GKI_CLANG_URI is set. This kernel "
                 "must be built with Google's Clang %s - see the comment in %s."
                 % (d.getVar("GKI_CLANG_VERSION"), d.getVar("FILE")))
    if clang_dir and not os.path.exists(os.path.join(clang_dir, "bin", "clang")):
        bb.fatal("GKI_CLANG_DIR %s has no bin/clang" % clang_dir)
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

# OE exports its own cross toolchain and flags into every task; none of it
# applies here and some of it is fatal. kernel.bbclass unsets the same set.
gki_clear_oe_env() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    unset CC CXX CPP LD AR NM STRIP OBJCOPY OBJDUMP RANLIB READELF
    unset HOSTCC HOSTCXX BUILD_CC BUILD_CXX MACHINE
}

# Four things here that the android14-6.1 recipe in meta-android does not need.
# All four are properties of 5.10's kbuild, not of this device, so they will
# recur on any other android12-5.10 port:
#
#   CROSS_COMPILE         5.10's Makefile derives clang's --target from it -
#                         "CLANG_FLAGS += --target=$(notdir $(CROSS_COMPILE:%-=%))"
#                         guarded by "ifneq ($(CROSS_COMPILE),)". LLVM=1 alone
#                         does NOT imply a target here; 6.1 works it out from
#                         ARCH, 5.10 does not. Without it clang compiles for the
#                         x86_64 host and asm-offsets.c fails with "register
#                         'sp' unsuitable for global register variables on this
#                         target" - which reads like a kernel bug and is not.
#                         No GNU cross-binutils are needed: only the triple is
#                         used, everything else is LLVM.
#   LLVM_IAS=1            5.10 picks -fintegrated-as vs -fno-integrated-as off
#                         this and defaults to the latter, which would want a
#                         GNU assembler we deliberately do not have.
#   CROSS_COMPILE_COMPAT  CONFIG_COMPAT_VDSO=y here (as in the stock config), so
#                         the 32-bit vDSO is built too and needs its own triple.
#   PYTHON=python3        5.10 still defaults PYTHON to "python", and
#                         link-vmlinux.sh calls ${PYTHON} scripts/jobserver-exec
#                         - so the whole kernel compiles and then the vmlinux
#                         link dies with "python: not found" on any host with
#                         only python3. Droidian's CI answers this with
#                         "ln -s python2 /usr/bin/python", which is both a
#                         host-wide change and the wrong interpreter:
#                         jobserver-exec parses clean as python3 (checked).
#
# The first three match Google's build.config.aarch64 exactly.
#
# MTK_PLATFORM_VERSION is a fifth addition, and this one IS device-specific.
# MediaTek's Mali tree has a bug: drivers/gpu/.../midgard/Makefile line 21 does
#
#     MTK_PLATFORM_VERSION := $(CONFIG_MTK_PLATFORM:"%"=%)
#
# without exporting it, and the two SIBLING directories
# memory_group_manager/ and protected_memory_allocator/ then use it to name
# their modules:
#
#     obj-m += mali_mgm_$(MTK_PLATFORM_VERSION).o
#     obj-m += mali_prot_alloc_$(MTK_PLATFORM_VERSION).o
#
# kbuild descends into them with the variable unset, so we produced
# "mali_mgm_.ko" and "mali_prot_alloc_.ko" against the stock
# "mali_mgm_mt6789.ko" and "mali_prot_alloc_mt6789.ko". (mali_kbase escapes it
# because drivers/gpu/mediatek/Makefile *exports* MTK_PLATFORM, which is what
# names that one.) Android's build passes the variable in the environment, which
# is why the vendor never hit it.
#
# Harmless while we ship no modules of our own - the device loads its own
# correctly-named stock pair - but it is exactly the silent failure
# kmi-crc-matching.md warns about under "DO match the layout exactly": the
# vendor's modules.load and modules.dep address modules by name, so a future
# module override would quietly skip these two and load the vendor's binaries
# against our kernel instead.
KERNEL_MAKE = "make -C ${S} O=${B} LLVM=1 LLVM_IAS=1 ARCH=arm64 \
    CROSS_COMPILE=aarch64-linux-gnu- CROSS_COMPILE_COMPAT=arm-linux-gnueabi- \
    PYTHON=python3 MTK_PLATFORM_VERSION=mt6789"

do_configure:prepend() {
    # The other half of the vermagic match; files/vermagic.cfg explains why it
    # matters. scripts/setlocalversion ends with
    #
    #     if test "${LOCALVERSION+set}" != "set"; then
    #             scm=$(scm_version --short)
    #             res="$res${scm:++}"
    #     fi
    #
    # i.e. a "+" whenever the tree is not sitting on a clean annotated tag -
    # which a git checkout of a branch never is. That single character is enough
    # to miss the match ("...gfcab0aff02db+" != "...gfcab0aff02db") and then not
    # one stock module loads. An empty .scmversion short-circuits scm_version()
    # to the empty string, so ${scm:++} expands to nothing.
    : > ${S}/.scmversion
}

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    ${KERNEL_MAKE} ${Q25_KERNEL_CONFIGS}
    # Vermagic first, and unconditionally - including for the
    # LUNEOS_KERNEL_FRAGMENT="0" baseline. It belongs to "reproduce the vendor's
    # kernel", not to the LuneOS delta, so the baseline must carry it too or the
    # baseline is not a baseline.
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
        ${B}/.config ${UNPACKDIR}/frag/vermagic.cfg
    ${KERNEL_MAKE} olddefconfig
    if [ "${KERNEL_LTO_THIN}" = "1" ]; then
        printf '%s\n' '# CONFIG_LTO_CLANG_FULL is not set' 'CONFIG_LTO_CLANG_THIN=y' \
            > ${B}/thinlto.cfg
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} ${B}/.config ${B}/thinlto.cfg
        ${KERNEL_MAKE} olddefconfig
    fi
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
            ${B}/.config ${UNPACKDIR}/frag/luneos.cfg
        ${KERNEL_MAKE} olddefconfig
    fi
}

do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-I${STAGING_INCDIR_NATIVE} -Wno-incompatible-pointer-types-discards-qualifiers" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.gz modules
}

# Ship our patched keyboard driver in the initramfs, where it REPLACES the
# vendor's copy. Without this the patch is dead code: bbqX0kbd.ko is in the stock
# vendor_boot ramdisk's modules.load, so the device would load the vendor's
# binary and never see our symbol layers.
#
# The mechanism is the MP01's, for its patched mediatek-drm.ko. The bootloader
# concatenates vendor_boot's ramdisk and then ours, and later cpio entries win,
# so a file at lib/modules/bbqX0kbd.ko in this image substitutes for the
# vendor's; init.sh's load_kernel_modules() then inserts ours in the vendor's own
# dependency order. /override/modules cannot do it - those are inserted BEFORE
# the vendor set.
#
# This is one 66 KB module, not the 344-module wholesale copy q25.conf explains
# we must not do: the boot image goes from ~22.6 MB to ~22.7 MB, nowhere near
# the ~28 MB at which the MP01 measured lk refusing to load one at all.
#
# q25.conf adds this package to ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL.
do_install() {
    install -d ${D}${nonarch_base_libdir}/modules
    install -m 0644 ${B}/drivers/input/keyboard/bbqX0kbd/bbqX0kbd.ko \
        ${D}${nonarch_base_libdir}/modules/bbqX0kbd.ko
    # 488 KB unstripped against the vendor's 66 KB, and an unstripped .ko also
    # trips the debug-files QA check. Stripped with the toolchain that built it:
    # OE's strip does not understand a module built outside its cross
    # environment.
    ${@gki_clang_bin(d).split(':')[0]}/llvm-strip --strip-debug \
        ${D}${nonarch_base_libdir}/modules/bbqX0kbd.ko
}

FILES:${PN} = "${nonarch_base_libdir}/modules/bbqX0kbd.ko"

# We strip it ourselves, above; OE's strip and debug-split cannot.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
# A kernel module legitimately records the path it was built in.
INSANE_SKIP:${PN} += "buildpaths"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/arch/arm64/boot/Image.gz ${DEPLOYDIR}/Image.gz
    # The input to check-kmi.sh, and the only way to tell without a device
    # whether a config change just stopped the vendor's modules from loading.
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
    # What the stock modules will be checked against. If this does not read
    # exactly 5.10.198-android12-9-gfcab0aff02db, not one of them will insmod -
    # see files/vermagic.cfg. Cheaper to read than to discover on the device.
    install -m 0644 ${B}/include/config/kernel.release ${DEPLOYDIR}/kernel.release
}
addtask deploy after do_compile before do_build
