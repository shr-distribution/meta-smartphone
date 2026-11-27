# SPDX-License-Identifier: MIT
#
# Build out-of-tree kernel modules with the same Clang as a Clang-built kernel.
#
# kernel-arch.bbclass hard-assigns KERNEL_CC/LD/AR/OBJCOPY to OE's GCC cross
# toolchain. A kernel recipe that swaps those for Clang (pioneer, radon,
# sunfish) does so for itself only, so make-mod-scripts and every module.bbclass
# recipe still build against that kernel with GCC. Two things then go wrong:
#
#  - make-mod-scripts' "make prepare" re-runs syncconfig with GCC and rewrites
#    the staged ${STAGING_KERNEL_BUILDDIR}/.config: CC_IS_CLANG, LTO_CLANG and
#    CFI_CLANG drop out, and every module after it is built for a config that
#    is not the running kernel's.
#  - The module is GCC code in a Clang kernel. On pioneer that fails the build
#    (this 4.19 tree defines __copy() for Clang only, so module_init trips
#    -Werror=missing-attributes). On radon it builds silently and is worse: the
#    module has no __cfi_check, and a CFI_CLANG kernel without CFI_PERMISSIVE
#    treats any call into such a module as a CFI failure (kernel/cfi.c,
#    "Don't allow unchecked modules").
#
# Opt in from the machine configuration:
#
#   KERNEL_TOOLCHAIN = "clang"
#   INHERIT += "kernel-clang-toolchain"
#
# and set GKI_CLANG_VERSION in the machine configuration too, not only in the
# kernel recipe: these recipes resolve the compiler from it the same way the
# kernel does (lib/halium/gkiclang.py), and depend on the same
# android-clang-<rev>-native. The settings are applied in anonymous python,
# after kernel-arch.bbclass's assignments, and only to the recipes that run
# kbuild against the kernel, so the prebuilt Clang does not go in front of PATH
# for every other recipe of the machine.
#
# A staged .config that a GCC make-mod-scripts has already rewritten does not
# heal: LTO_CLANG and CFI_CLANG default to n, so syncconfig with Clang does not
# bring them back. Re-stage it from the kernel (its do_shared_workdir) before
# the first build with this class.

KERNEL_CLANG_EXTRA_OEMAKE ?= "CLANG_TRIPLE=aarch64-linux-gnu- LLVM_IAS=1 \
    AR=llvm-ar NM=llvm-nm OBJDUMP=llvm-objdump READELF=llvm-readelf"

python __anonymous() {
    if d.getVar('KERNEL_TOOLCHAIN') != 'clang':
        return
    if not (bb.data.inherits_class('module', d)
            or d.getVar('PN') == 'make-mod-scripts'):
        return

    if not d.getVar('GKI_CLANG_VERSION') and not d.getVar('GKI_CLANG_DIR'):
        bb.fatal("KERNEL_TOOLCHAIN is clang but neither GKI_CLANG_VERSION nor "
                 "GKI_CLANG_DIR is set; set GKI_CLANG_VERSION in the machine "
                 "configuration to the kernel's")

    # Clang only: these recipes never used the kernel-build-tools.
    d.appendVar('DEPENDS', ' ' + ' '.join(
        dep for dep in halium.gkiclang.depends(d)
        if dep.startswith('android-clang-')))
    d.prependVar('PATH', '${@halium.gkiclang.clang_root(d)}/bin:')

    # kernel-arch.bbclass's flags minus -fcanon-prefix-map, which only GCC 12+
    # knows; the same KERNEL_CC the Clang kernel recipes use.
    d.setVar('KERNEL_CC', "clang ${HOST_CC_KERNEL_ARCH} "
             "${@' '.join(f for f in (d.getVar('DEBUG_PREFIX_MAP') or '').split() if f != '-fcanon-prefix-map')} "
             "-ffile-prefix-map=${STAGING_KERNEL_DIR}=${KERNEL_SRC_PATH} "
             "-ffile-prefix-map=${STAGING_KERNEL_BUILDDIR}=${KERNEL_SRC_PATH}")
    d.setVar('KERNEL_LD', 'ld.lld')
    d.setVar('KERNEL_AR', 'llvm-ar')
    d.setVar('KERNEL_OBJCOPY', 'llvm-objcopy')
    d.setVar('KERNEL_STRIP', 'llvm-strip')
    d.appendVar('EXTRA_OEMAKE', ' ${KERNEL_CLANG_EXTRA_OEMAKE}')
}
