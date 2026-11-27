SUMMARY = "Android Common Kernel (GKI) android15-6.6, with the LuneOS config fragment"
DESCRIPTION = "\
One kernel per KMI, not per device: android15-6.6 serves every device whose \
vendor modules were built against that KMI - the Motorola moto g06 (lagos, \
MT6768) today. \
\
The android15-6.6 sibling of linux-halium-gki_6.1.bb; read that recipe for \
why this is built with Google's pinned Clang and plain kbuild rather than the \
OE cross toolchain. The device keeps its own stock vendor_boot, vendor_dlkm \
and system_dlkm modules and we only replace the GKI core, so the exported-symbol \
CRCs must stay exactly what those modules were built against. \
\
Verified 2026-11-09 host-side against the lagos VVOB35.78-188 firmware: \
gki_defconfig at this commit reproduces the stock IKCONFIG, and with the \
LuneOS fragment kmi-crc-check.py finds 427 of the 428 stock modules \
load-compatible, with 0 exported-symbol CRCs different from the stock-config \
build. The one failure, mtk_em.ko, imports calc_eff_hook, which neither the \
stock kernel nor any stock module exports, so it fails on stock firmware too. \
"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

# The Clang the stock lagos kernel was built with, read from its version
# string: "Android (11368308, +pgo, +bolt, +lto, +mlgo, based on r510928) clang
# version 18.0.0 (... 477610d4d0d988e69dbc3fae4fe86bff3f07f2b5)", which
# AOSP's clang-r510928 prints exactly. gki-clang.bbclass fetches it.
GKI_CLANG_VERSION ?= "r510928"
# pahole for CONFIG_DEBUG_INFO_BTF: the same prebuilts/kernel-build-tools the
# 6.1 recipe uses (pahole 1.23) builds this tree, given uncompressed DWARF - see
# DEBUG_INFO_COMPRESSED_NONE in the fragment.
GKI_BUILD_TOOLS ?= "gs-6.1"
GKI_REQUIRE_PAHOLE = "1"

# The fragment is per KMI, so it lives in its own directory ahead of the 6.1
# recipe's files/.
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}-6.6:"

SRC_URI = "\
    git://android.googlesource.com/kernel/common;protocol=https;nobranch=1;name=kernel \
    file://luneos_defconfig \
"
KMI_BRANCH ?= "android15-6.6"
# The exact commit of the stock lagos kernel: its release is
# 6.6.118-android15-8-g350f378eb496-ab14943902-4k. First in the release tags
# android15-6.6-2026-01_r15 onwards; nobranch=1 because release tags are not
# reachable from the branch tip (see the 6.1 recipe).
SRCREV_kernel = "350f378eb496c856c82d81aace74f992c9e2041e"
SRCREV_FORMAT = "kernel"

PV = "6.6.118+git"
B = "${WORKDIR}/build"

# "0" builds gki_defconfig verbatim - the stock configuration - as the control
# experiment for a failing KMI check (kmi-crc-matching.md "Step 6").
LUNEOS_KERNEL_FRAGMENT ?= "1"

DEPENDS = "elfutils-native openssl-native bc-native bison-native flex-native lz4-native python3-native"

inherit deploy gki-clang

COMPATIBLE_MACHINE = "^(lagos)$"
PACKAGE_ARCH = "${MACHINE_ARCH}"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

gki_clear_oe_env() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    unset CC CXX CPP LD AR NM STRIP OBJCOPY OBJDUMP RANLIB READELF
    unset HOSTCC HOSTCXX BUILD_CC BUILD_CXX MACHINE
}

KERNEL_MAKE = "make -C ${S} O=${B} LLVM=1 LLVM_IAS=1 ARCH=arm64"

do_configure() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    mkdir -p ${B}
    ${KERNEL_MAKE} gki_defconfig
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        ${S}/scripts/kconfig/merge_config.sh -m -O ${B} \
            ${B}/.config ${UNPACKDIR}/luneos_defconfig
    else
        bbwarn "LUNEOS_KERNEL_FRAGMENT=0: building the stock gki_defconfig (KMI baseline), not a LuneOS kernel"
    fi
    ${KERNEL_MAKE} olddefconfig

    # merge_config.sh drops what Kconfig does not know without failing, and
    # kconfig drops toolchain-probed symbols silently (kmi-crc-matching.md
    # "Step 5"). Check the result, not the request.
    grep -qx 'CONFIG_DEBUG_INFO_BTF_MODULES=y' ${B}/.config || \
        bbfatal "DEBUG_INFO_BTF_MODULES dropped out of .config (pahole missing?): struct module would no longer match the stock modules"
    if [ "${LUNEOS_KERNEL_FRAGMENT}" = "1" ]; then
        missing=$(grep -E '^(# )?CONFIG_[A-Za-z0-9_]+[= ]' ${UNPACKDIR}/luneos_defconfig | \
            while read -r line; do
                grep -qxF "$line" ${B}/.config || echo "$line"
            done)
        [ -z "$missing" ] || \
            bbfatal "luneos_defconfig lines that did not survive into .config: $missing"
    fi
}

do_compile() {
    gki_clear_oe_env
    export PATH="${@gki_clang_bin(d)}:$PATH"
    # resolve_btfids builds libbpf against elfutils from the native sysroot.
    # -idirafter, not -I, so the native sysroot cannot shadow the tree's own
    # tools/include (device-brax3.md), and the one libbpf-vs-elfutils
    # const-correctness warning that -Werror trips on is named explicitly.
    ${KERNEL_MAKE} -j${@oe.utils.cpu_count()} \
        HOSTCFLAGS="-idirafter ${STAGING_INCDIR_NATIVE} -Wno-incompatible-pointer-types-discards-qualifiers" \
        HOSTLDFLAGS="-L${STAGING_LIBDIR_NATIVE} -Wl,-rpath,${STAGING_LIBDIR_NATIVE}" \
        Image.lz4 Image.gz modules
}

do_install[noexec] = "1"

do_deploy() {
    install -d ${DEPLOYDIR}
    # Both compressions: a machine picks one with GKI_KERNEL_IMAGE_NAME (lagos
    # ships gzip, like its stock boot.img).
    install -m 0644 ${B}/arch/arm64/boot/Image.lz4 ${DEPLOYDIR}/Image.lz4
    install -m 0644 ${B}/arch/arm64/boot/Image.gz ${DEPLOYDIR}/Image.gz
    install -m 0644 ${B}/Module.symvers ${DEPLOYDIR}/Module.symvers
    install -m 0644 ${B}/.config ${DEPLOYDIR}/kernel-config
    install -m 0644 ${B}/include/config/kernel.release ${DEPLOYDIR}/kernel.release
}
addtask deploy after do_compile before do_build
