# SPDX-License-Identifier: MIT
#
# Where an Android kernel's pinned Clang (and Google's kernel-build-tools) come
# from. Shared by gki-clang.bbclass (the kernel recipes) and
# kernel-clang-toolchain.bbclass (the module recipes built against them), so
# the two cannot resolve to different compilers.
#
# The layer says which toolchain a kernel needs; the build host says nothing:
#
#   GKI_CLANG_VERSION    e.g. "r416183b". Set by the kernel recipe, or by the
#                        machine .conf where module recipes need it too
#                        (KERNEL_TOOLCHAIN = "clang" machines).
#   GKI_BUILD_TOOLS      e.g. "android12-5.10": which kernel-build-tools
#                        snapshot goes on PATH, for pahole. Empty for none.
#
# Both resolve to android-clang-<rev>-native and
# android-kernel-build-tools-<name>-native, fetched by commit from Google and
# staged into the recipe's native sysroot.
#
# Local overrides, for hosts that cannot run Google's x86-64 binaries or are
# offline with a repo-synced tree. Setting one drops the matching dependency:
#
#   GKI_CLANG_DIR        a clang-<rev> directory (or any LLVM prefix with
#                        bin/clang, for a recipe that sets no version)
#   GKI_BUILD_TOOLS_DIR  a kernel-build-tools linux-x86 directory

import os

import bb

CLANG_TOOLS = ("clang", "ld.lld", "llvm-ar", "llvm-objcopy")


def clang_root(d):
    override = (d.getVar("GKI_CLANG_DIR") or "").rstrip("/")
    if override:
        return override
    return os.path.join(d.getVar("STAGING_LIBDIR_NATIVE"), "android-clang",
                        "clang-" + (d.getVar("GKI_CLANG_VERSION") or ""))


def build_tools_root(d):
    override = (d.getVar("GKI_BUILD_TOOLS_DIR") or "").rstrip("/")
    if override:
        return override
    name = d.getVar("GKI_BUILD_TOOLS")
    if not name:
        return ""
    return os.path.join(d.getVar("STAGING_LIBDIR_NATIVE"),
                        "android-kernel-build-tools", name)


def path(d):
    """The directories to put in front of PATH: Clang, then the build tools."""
    bins = [os.path.join(clang_root(d), "bin")]
    tools = build_tools_root(d)
    if tools:
        bins.append(os.path.join(tools, "bin"))
    return ":".join(bins)


def depends(d):
    """The native recipes to add to DEPENDS. Never fatal: it runs at parse
    time for every machine, including ones the recipe is not compatible with;
    check() reports what is missing."""
    deps = []
    version = d.getVar("GKI_CLANG_VERSION")
    if version and not d.getVar("GKI_CLANG_DIR"):
        deps.append("android-clang-%s-native" % version)
    tools = d.getVar("GKI_BUILD_TOOLS")
    if tools and not d.getVar("GKI_BUILD_TOOLS_DIR"):
        deps.append("android-kernel-build-tools-%s-native" % tools)
    return deps


def check(d):
    """Fail before configure, not 20 minutes into the build."""
    version = d.getVar("GKI_CLANG_VERSION")
    override = (d.getVar("GKI_CLANG_DIR") or "").rstrip("/")
    if not version and not override:
        bb.fatal("Neither GKI_CLANG_VERSION nor GKI_CLANG_DIR is set for %s. "
                 "This kernel must be built with a pinned Clang - see the "
                 "comment in %s." % (d.getVar("PN"), d.getVar("FILE")))
    root = clang_root(d)
    for tool in CLANG_TOOLS:
        if not os.path.exists(os.path.join(root, "bin", tool)):
            bb.fatal("%s has no bin/%s" % (root, tool))
    # An override that is visibly a different Android Clang than the one the
    # recipe pins is the mistake this whole arrangement exists to stop. A
    # distro LLVM prefix (/usr/lib/llvm-19) is the caller's own choice.
    base = os.path.basename(override)
    if version and base.startswith("clang-r") and base != "clang-" + version:
        bb.fatal("GKI_CLANG_DIR %s is not clang-%s, which %s is built with."
                 % (override, version, d.getVar("PN")))
    if d.getVar("GKI_REQUIRE_PAHOLE") == "1":
        tools = build_tools_root(d)
        if not tools or not os.path.exists(os.path.join(tools, "bin", "pahole")):
            bb.fatal("%s needs pahole for CONFIG_DEBUG_INFO_BTF: set "
                     "GKI_BUILD_TOOLS (or GKI_BUILD_TOOLS_DIR)."
                     % d.getVar("PN"))
