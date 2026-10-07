# Build fixes for the Linux 3.4 Halium kernels (tenderloin-halium,
# hammerhead-halium, mako-halium), on top of halium-kernel.inc.
#
# GCC 15 defaults to -std=gnu23, where bool, true and false are keywords. 3.4
# was written for the gnu89 that GCC defaulted to when it was current, and it
# typedefs bool and uses false as an enumerator, so the very first translation
# unit fails:
#
#   include/linux/stddef.h: error: cannot use keyword 'false' as enumeration constant
#   include/linux/types.h:  error: 'bool' cannot be defined via 'typedef'
#
# gnu89 rather than a newer standard because it is also what the tree's inline
# semantics assume. This only touches the target compiler; the host tools are
# handled with BUILD_CFLAGS where a tree needs it.
#
# -fno-pic -fno-pie as well: the cross GCC is configured --enable-default-pie,
# and 3.4's kbuild predates the -fno-PIE that later kernels add themselves. The
# kernel image links statically so it does not notice, but every module is
# built position independent and needs a GOT the module loader cannot provide:
#
#   bcmdhd: Unknown symbol _GLOBAL_OFFSET_TABLE_ (err 0)
#
# bcmdhd.ko from a build without this has 456 R_ARM_GOT_BREL relocations and an
# undefined _GLOBAL_OFFSET_TABLE_, so the WiFi driver can never load.
KERNEL_CC:append = " -std=gnu89 -fno-pic -fno-pie"

# The host tools are a separate problem. scripts/unifdef.c declares
# "static bool constexpr;" and assigns to it, and C23 made constexpr a keyword,
# so with a host GCC that defaults to -std=gnu23 do_install fails in
# "make headers_install":
#
#   scripts/unifdef.c:206:1: error: 'constexpr' in empty declaration
#   scripts/unifdef.c:880:27: error: expected identifier or '(' before '=' token
#
# kernel.bbclass passes HOSTCFLAGS="${BUILD_CFLAGS}", so pinning the host C
# standard here is enough. Same fix as the xiaomi kernels.
BUILD_CFLAGS:append = " -std=gnu17"

# arch/arm/tools/gen-mach-types stamps an absolute path under TMPDIR into the
# banner comment of mach-types.h:
#
#   * This was automagically generated from <TMPDIR>/.../arch/arm/tools/mach-types!
#
# On 3.4 the file is include/generated/mach-types.h; later kernels moved it
# under arch/arm/include/generated/asm. It ships in the -src package and trips
# the [buildpaths] QA check. Rewrite the banner; the defines are unchanged.
#
# In do_install rather than do_compile so that changing it does not rebuild the
# kernel: the -src package is assembled from ${B} at packaging time.
do_install:append() {
    for f in ${B}/include/generated/mach-types.h \
             ${B}/arch/arm/include/generated/asm/mach-types.h; do
        if [ -f "$f" ]; then
            sed -i 's|generated from .*/arch/arm/tools/mach-types!|generated from arch/arm/tools/mach-types!|' "$f"
        fi
    done
}

# Kernel side of running the 16.0 GSI on these pre-Treble devices (see
# android-system-image-legacy-gsi.inc). A machine that opts in sets
# HALIUM_LEGACY_GSI = "1", and only then does the kernel change: the default
# machines build the kernel they always have.
#
# Android 16's bionic implements rename() as renameat2(..., 0) with no ENOSYS
# fallback, and 3.4 has no renameat2.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
# A kernel whose branch already has the commit (KERNEL_HAS_RENAMEAT2 = "1") must not apply it again.
SRC_URI:append = "${@' file://0001-ARM-add-renameat2-for-the-flags-0-case.patch' if d.getVar('HALIUM_LEGACY_GSI') == '1' and d.getVar('KERNEL_HAS_RENAMEAT2') != '1' else ''}"

# clock_gettime64 and clock_getres_time64, for every LuneOS userspace rather than only the GSI: glibc
# with 64-bit time_t calls clock_gettime64 first and falls back to clock_gettime on ENOSYS each time,
# so on a 3.4 kernel (no time64 syscalls, no ARM vDSO clock) every time query costs two system calls.
# A recipe opts in with KERNEL_ADD_CLOCK_TIME64 = "1" once the patch applies to its tree; a tree with
# memfd_create at 385 needs its own variant of the table hunk.
SRC_URI:append = "${@' file://0002-ARM-add-clock_gettime64-and-clock_getres_time64.patch' if d.getVar('KERNEL_ADD_CLOCK_TIME64') == '1' else ''}"
