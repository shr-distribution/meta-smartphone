# SPDX-License-Identifier: MIT
#
# Build an Android kernel with the Clang it is pinned to.
#
# The recipe (or, for KERNEL_TOOLCHAIN = "clang" machines, the machine .conf)
# names the toolchain; this class fetches it as a native recipe, puts it on
# PATH and checks it before configure. Nothing about it belongs in local.conf.
#
#   GKI_CLANG_VERSION = "r416183b"      -> android-clang-r416183b-native
#   GKI_BUILD_TOOLS = "android12-5.10"  -> android-kernel-build-tools-*-native
#   GKI_REQUIRE_PAHOLE = "1"            fail early if that has no pahole
#
# GKI_CLANG_DIR / GKI_BUILD_TOOLS_DIR still override with a local tree (an
# arm64 build host, an offline one); see lib/halium/gkiclang.py.
#
# Recipes that drive kbuild by hand use gki_clang_bin(d) in their tasks;
# kernel.bbclass-based ones put ${GKI_CLANG_BINDIR} on PATH.

GKI_CLANG_VERSION ?= ""
GKI_CLANG_DIR ?= ""
GKI_BUILD_TOOLS ?= ""
GKI_BUILD_TOOLS_DIR ?= ""
GKI_REQUIRE_PAHOLE ?= "0"

GKI_CLANG_BINDIR = "${@halium.gkiclang.clang_root(d)}/bin"

def gki_clang_bin(d):
    return halium.gkiclang.path(d)

python __anonymous() {
    deps = halium.gkiclang.depends(d)
    if deps:
        d.appendVar('DEPENDS', ' ' + ' '.join(deps))
}

python do_check_toolchain() {
    halium.gkiclang.check(d)
}
addtask check_toolchain after do_prepare_recipe_sysroot before do_configure
