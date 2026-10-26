SUMMARY = "BraX3 kernel - MediaTek mgk 5.15.209 (android13-5.15 KMI gen 8, MT6835) + the LuneOS fragment"
DESCRIPTION = "\
The BraX3's own kernel, built from the tree Brax Technologies publishes for \
BaseOS, not from the Android Common Kernel. Same arrangement as the Q25 and the \
MP01 (meta-zinwa, meta-minimal): MediaTek ships its 'mgk' GKI derivative, the \
stock vendor_boot and vendor_dlkm modules stay, and we replace only the kernel - \
so the baseline to reproduce is MediaTek's/Brax's, not Google's. \
\
Measured, not assumed: this tree with brax3_defconfig, ThinLTO and the stock \
vermagic, built with clang r450784e, scores 0 of 774 stock BaseOS modules and \
0 of 377 stock iodeOS modules failing kmi-crc-check.py. Built once per vendor \
(MACHINE=brax3 for BaseOS, brax3-iode for iodeOS); only the release string \
differs, see BRAX3_VENDOR. \
\
Gate, every time, before flashing: kmi-crc-check.py <deployed Module.symvers> \
against the stock vendor_boot + vendor_dlkm modules -> 0 would fail. \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# Same Clang the stock kernel was built with. Its version string says
# "Android (8508608, based on r450784e) clang version 14.0.7", which is also
# CLANG_VERSION in the tree's build.config.constants and
# TARGET_KERNEL_CLANG_VERSION in BaseOS's BoardConfig.mk. Set
# GKI_CLANG_DIR:brax3 in local.conf; AOSP's prebuilts/clang/host/linux-x86 at
# any android-13/-14 tag carries clang-r450784e.
GKI_CLANG_VERSION ?= "r450784e"
GKI_CLANG_DIR ?= ""
GKI_CLANG_URI ?= ""

# Brax's own tree for BaseOS 1.0, published as one squashed commit. It should
# move to shr-distribution/linux as brax3/5.15.209/lune like the MP01's, so
# fixes become commits there and a SRCREV bump rather than files here.
SRC_URI = "\
    git://git.os-source.co/Brax3_BaseOs/android_kernel_mt6835.git;protocol=https;branch=baseos-1.0;name=kernel \
    ${@(d.getVar('GKI_CLANG_URI') + ';name=clang;subdir=clang') if d.getVar('GKI_CLANG_URI') else ''} \
    file://vermagic-${BRAX3_VENDOR}.cfg;subdir=frag \
    file://luneos.cfg;subdir=frag \
    git://git.os-source.co/Brax3_BaseOs/android_vendor_mediatek_kernel_modules.git;protocol=https;branch=baseos-1.0;name=vendormodules;destsuffix=vendor-kernel-modules \
    file://0001-mali-r38p1-find-the-context-s-task-without-a-pid-nam.patch;patchdir=${UNPACKDIR}/vendor-kernel-modules \
"
SRCREV_kernel = "815b9e54f14ede5c702869dff1f0ddcebba4476b"
# The Mali driver is not in the kernel tree: the stock mali_kbase_mt6835.ko
# ("r38p1-01eac0") is built out of tree from BaseOS's
# vendor/mediatek/kernel_modules, gpu/mt6835 -> gpu_mali/mali_valhall/mali-r38p1.
SRCREV_vendormodules = "724de3483929a68439d15a9815f2c53783058f98"
SRCREV_FORMAT = "kernel_vendormodules"

PV = "5.15.209+git"
B = "${WORKDIR}/build"

# pahole is the one dependency the MT6789 kernels did not have. The BraX3's
# stock config sets CONFIG_DEBUG_INFO_BTF and DEBUG_INFO_BTF_MODULES, and the
# second one adds btf_data_size/btf_data to struct module
# (include/linux/module.h). Without pahole on PATH, olddefconfig silently drops
# both, struct module changes size, module_layout moves, and all 774 stock
# modules fail - measured. Any pahole >= 1.19 satisfies PAHOLE_HAS_SPLIT_BTF;
# PAHOLE_VERSION itself feeds no struct, so meta-oe's version is fine (stock
# was 1.25; 1.31 measured 0/774).
DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native pahole-native"

inherit deploy

COMPATIBLE_MACHINE = "^(brax3|brax3-iode)$"

# Which vendor's modules this kernel must insmod, set by the machine:
#   baseos  5.15.209-android13-8-gdd511ad2a748  (the tree's own sublevel)
#   iode    5.15.211-android13-8-g63db33e5daa0  (sublevel forced on the make
#           command line; the tree's Makefile says 209)
# The two module sets are one KMI; vermagic-<vendor>.cfg explains.
BRAX3_VENDOR ?= "baseos"
BRAX3_KERNEL_SUBLEVEL = "${@'211' if d.getVar('BRAX3_VENDOR') == 'iode' else ''}"
BRAX3_KERNEL_RELEASE = "${@{'baseos': '5.15.209-android13-8-gdd511ad2a748', 'iode': '5.15.211-android13-8-g63db33e5daa0'}[d.getVar('BRAX3_VENDOR')]}"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

# brax3_defconfig is a full generated config, not a fragment chain like the
# Q25's: BaseOS builds with TARGET_KERNEL_CONFIG := brax3_defconfig alone, and
# it reproduces the stock IKCONFIG except for the LTO mode, LOCALVERSION and the
# pahole version (scripts/diffconfig against extract-ikconfig of the shipped
# kernel).
#
# LUNEOS_KERNEL_FRAGMENT = "0" builds the stock baseline instead. Baseline
# first, then the delta, whenever a KMI check fails.
BRAX3_KERNEL_CONFIGS ?= "brax3_defconfig"
LUNEOS_KERNEL_FRAGMENT ?= "1"
# ThinLTO instead of stock full LTO, which costs a long single-threaded link.
# KMI-neutral: the baseline measured 0/774 with ThinLTO.
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

gki_clear_oe_env() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    unset CC CXX CPP LD AR NM STRIP OBJCOPY OBJDUMP RANLIB READELF
    unset HOSTCC HOSTCXX BUILD_CC BUILD_CXX MACHINE
}

# The same kbuild variables as linux-zinwa-q25 (that recipe explains each);
# they match the tree's own build.config.mtk.aarch64. MTK_PLATFORM_VERSION names
# the Mali sibling modules (mali_mgm_mt6835.ko, mali_prot_alloc_mt6835.ko) the
# way Android's build does.
KERNEL_MAKE = "make -C ${S} O=${B} LLVM=1 LLVM_IAS=1 ARCH=arm64 \
    CROSS_COMPILE=aarch64-linux-gnu- CROSS_COMPILE_COMPAT=arm-linux-gnueabi- \
    PYTHON=python3 MTK_PLATFORM_VERSION=mt6835 \
    ${@('SUBLEVEL=' + d.getVar('BRAX3_KERNEL_SUBLEVEL')) if d.getVar('BRAX3_KERNEL_SUBLEVEL') else ''}"

do_configure:prepend() {
    # Empty .scmversion: otherwise setlocalversion appends "+" and the vermagic
    # misses (vermagic-*.cfg).
    : > ${S}/.scmversion
}

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    ${KERNEL_MAKE} ${BRAX3_KERNEL_CONFIGS}
    # Vermagic first, and unconditionally - it belongs to the baseline.
    ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
        ${B}/.config ${UNPACKDIR}/frag/vermagic-${BRAX3_VENDOR}.cfg
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
    # The pahole trap above, caught here rather than as 774 failed modules.
    grep -q '^CONFIG_DEBUG_INFO_BTF_MODULES=y' ${B}/.config || \
        bbfatal "CONFIG_DEBUG_INFO_BTF_MODULES dropped out of the config - is pahole on PATH? Without it struct module no longer matches the stock modules."
}

# -idirafter, not -I, for the native sysroot: pahole-native brings libbpf-native,
# whose linux/bpf.h and bpf/*.h would otherwise shadow the tree's own
# tools/include copies, and the host build of resolve_btfids then fails with
# "redefinition of 'bpf_core_relo'". After the tree's headers, the sysroot only
# supplies what the tree does not carry (libelf, openssl, zlib).
do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-idirafter ${STAGING_INCDIR_NATIVE} -Wno-incompatible-pointer-types-discards-qualifiers" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.lz4 modules
    # The Mali driver, out of tree against the kernel just built, with the
    # vendor's own make arguments (gpu/Makefile, the mt6835 entry of PARAMS).
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} M=${BRAX3_MALI_DIR} \
        MTK_PLATFORM_VERSION=mt6835 \
        LOCAL_MTK_GPU_VERSION=mali_valhall_r38p1_mt6835 \
        BUILD_RULE=OOT modules
}

BRAX3_MALI_DIR = "${UNPACKDIR}/vendor-kernel-modules/gpu/mt6835/mali_valhall/mali-r38p1/drivers/gpu/arm/midgard"

# Everything else stays stock: the stock modules all load against this kernel.
#
# The Mali driver, patched (0001) so that a GPU context can be created from
# inside a pid namespace - without it Android in Waydroid cannot start, as on
# the Q25 (meta-zinwa 0004). It is a vendor_dlkm module, so it goes in the
# rootfs, where mount-android.sh loads everything in vendor_dlkm-override/ ahead
# of the container, and the vendor's own modprobe then finds it already loaded.
# Its interface is the stock one - all 397 imported and 134 exported symbol
# CRCs match the vendor's mali_kbase_mt6835.ko, and those of BaseOS and iodeOS
# are identical; only the vermagic differs, which this machine's kernel gives.
do_install() {
    install -d ${D}${nonarch_base_libdir}/modules/vendor_dlkm-override
    install -m 0644 ${BRAX3_MALI_DIR}/mali_kbase_mt6835.ko \
        ${D}${nonarch_base_libdir}/modules/vendor_dlkm-override/mali_kbase_mt6835.ko
    # Stripped with the toolchain that built it: OE's strip does not understand
    # a module built outside its cross environment.
    ${@gki_clang_bin(d).split(':')[0]}/llvm-strip --strip-debug \
        ${D}${nonarch_base_libdir}/modules/vendor_dlkm-override/mali_kbase_mt6835.ko
}

# brax3.inc adds the override package to the rootfs; ${PN} itself carries nothing.
PACKAGES =+ "${PN}-vendor-dlkm-override"
FILES:${PN}-vendor-dlkm-override = "${nonarch_base_libdir}/modules/vendor_dlkm-override"
ALLOW_EMPTY:${PN} = "1"

# We strip it ourselves, above; OE's strip and debug-split cannot.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
# A kernel module legitimately records the path it was built in.
INSANE_SKIP:${PN}-vendor-dlkm-override += "buildpaths"

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${B}/arch/arm64/boot/Image.lz4 ${DEPLOYDIR}/Image.lz4
    # The input to kmi-crc-check.py.
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
    # Must equal the vendor's modules' release, or not one of them insmods.
    release=$(cat ${B}/include/config/kernel.release)
    [ "$release" = "${BRAX3_KERNEL_RELEASE}" ] || \
        bbfatal "kernel.release is $release, the ${BRAX3_VENDOR} modules need ${BRAX3_KERNEL_RELEASE}"
    install -m 0644 ${B}/include/config/kernel.release ${DEPLOYDIR}/kernel.release
}
addtask deploy after do_compile before do_build
