# Google's prebuilt Clang, one recipe per release, exactly as AOSP ships it.
#
# Android kernels are built with the Clang their KMI was frozen with, not with
# OE's: a different compiler is not validated against that KMI, and CRC or CFI
# drift only shows up on the device as "the stock vendor modules do not load".
# These recipes make that compiler an ordinary build input pinned by commit,
# instead of a path into someone's repo-synced tree, so a build is the same on
# every host and its sstate can be shared. gki-clang.bbclass and
# kernel-clang-toolchain.bbclass are the consumers; see lib/halium/gkiclang.py.
#
# Fetched from Google's own repository, platform/prebuilts/clang/host/linux-x86,
# by commit, with git checking out only the one clang-<rev> directory. Not
# gitiles' +archive tarballs: those are generated per request and their bytes
# (and so their sha256) change from one download to the next - measured, two
# fetches of the same directory and commit a few seconds apart differed in
# size - even though what they unpack to is identical. The commit hash is
# Google's own checksum of the content instead.
#
# Shallow: the repository holds every Clang Google ever shipped. At depth 1 a
# commit is ~0.6-2.9 GiB, fetched once into DL_DIR and shared by every recipe
# here that pins the same commit.
#
# x86-64 build hosts only: the prebuilts are x86-64 binaries. On any other host
# set GKI_CLANG_DIR, which takes this recipe out of the build.

SUMMARY = "Prebuilt Android Clang ${ANDROID_CLANG_VERSION} (AOSP prebuilts/clang/host/linux-x86)"
HOMEPAGE = "https://android.googlesource.com/platform/prebuilts/clang/host/linux-x86/"
LICENSE = "Apache-2.0-with-LLVM-exception & NCSA"
# NOTICE is byte-identical in r416183b, r450784e, r487747c and r510928.
LIC_FILES_CHKSUM = "file://NOTICE;md5=eeec5cfa0edfb54bfdba757236c7b531"

# Set by the version recipe: the clang-<rev> directory and a commit of the
# prebuilts repository that has it.
ANDROID_CLANG_VERSION ?= ""

SRC_URI = "git://android.googlesource.com/platform/prebuilts/clang/host/linux-x86;protocol=https;nobranch=1;subpath=clang-${ANDROID_CLANG_VERSION};destsuffix=clang-${ANDROID_CLANG_VERSION}"
S = "${UNPACKDIR}/clang-${ANDROID_CLANG_VERSION}"

BB_GIT_SHALLOW = "1"
BB_GIT_SHALLOW_DEPTH = "1"

inherit native

COMPATIBLE_HOST = "x86_64.*-linux"
INHIBIT_DEFAULT_DEPS = "1"
EXCLUDE_FROM_WORLD = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

# Staged at ${STAGING_LIBDIR_NATIVE}/android-clang/clang-<rev>, where
# lib/halium/gkiclang.py looks. Copied whole: bin/ runs against lib/, lib64/
# and the bundled python3 through $ORIGIN-relative paths.
do_install() {
    install -d ${D}${libdir}/android-clang
    cp -R --no-dereference --preserve=mode,links,timestamps ${S} \
        ${D}${libdir}/android-clang/clang-${ANDROID_CLANG_VERSION}
    rm -rf ${D}${libdir}/android-clang/clang-${ANDROID_CLANG_VERSION}/.git
}

# Google's binaries go in as shipped; a stripped or re-linked compiler is not
# the compiler the KMI was built with.
INHIBIT_SYSROOT_STRIP = "1"
# Nor does relocatable.bbclass get to rewrite their RPATHs: they are already
# $ORIGIN-relative, and its pass over them only warns ("Skipping RPATH /lib")
# on every module of the bundled python3.
SYSROOT_PREPROCESS_FUNCS:remove = "relocatable_binaries_preprocess"
