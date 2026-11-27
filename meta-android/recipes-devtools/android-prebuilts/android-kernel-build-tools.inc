# Google's kernel-build-tools prebuilts (kernel/prebuilts/build-tools,
# linux-x86/), pinned by commit. Wanted for bin/pahole: CONFIG_DEBUG_INFO_BTF
# needs it, and the KMI's BTF was generated with Google's, not a distro one.
# The whole bin/ goes on PATH, as it did when this was a local.conf path - it
# also carries lz4, dtc, depmod and friends, which therefore win over OE's.
#
# Fetched from Google's repository by commit, like android-clang.inc and for
# the same reason. x86-64 build hosts only; GKI_BUILD_TOOLS_DIR overrides.

SUMMARY = "Prebuilt Android kernel build tools (AOSP kernel/prebuilts/build-tools)"
HOMEPAGE = "https://android.googlesource.com/kernel/prebuilts/build-tools/"
# The bundle carries no licence file, and its tools come from many AOSP
# projects. This records pahole's (GPL-2.0-only, as meta-oe's pahole recipe
# has it), the tool it is staged for; the rest are not individually recorded.
# Nothing from it is installed on the target.
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

# Set by the snapshot recipe: its name (what GKI_BUILD_TOOLS selects) and commit.
ANDROID_BUILD_TOOLS_NAME ?= ""

SRC_URI = "git://android.googlesource.com/kernel/prebuilts/build-tools;protocol=https;nobranch=1;subpath=linux-x86;destsuffix=linux-x86"
S = "${UNPACKDIR}/linux-x86"

BB_GIT_SHALLOW = "1"
BB_GIT_SHALLOW_DEPTH = "1"

inherit native

COMPATIBLE_HOST = "x86_64.*-linux"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

# ${STAGING_LIBDIR_NATIVE}/android-kernel-build-tools/<name>, where
# lib/halium/gkiclang.py looks. bin/ finds lib64/ through $ORIGIN.
do_install() {
    install -d ${D}${libdir}/android-kernel-build-tools
    cp -R --no-dereference --preserve=mode,links,timestamps ${S} \
        ${D}${libdir}/android-kernel-build-tools/${ANDROID_BUILD_TOOLS_NAME}
    rm -rf ${D}${libdir}/android-kernel-build-tools/${ANDROID_BUILD_TOOLS_NAME}/.git
}

INHIBIT_SYSROOT_STRIP = "1"
# Nor does relocatable.bbclass get to rewrite their RPATHs: they are already
# $ORIGIN-relative, and its pass over them only warns ("Skipping RPATH /lib")
# on every module of the bundled python3.
SYSROOT_PREPROCESS_FUNCS:remove = "relocatable_binaries_preprocess"
