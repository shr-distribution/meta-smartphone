DESCRIPTION = "Hybris is a solution that commits hybris, by allowing us to use \
bionic-based HW adaptations in glibc systems"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://../LICENSE.Apache2;md5=3b83ef96387f14655fc854ddc3c6bd57"

# TheKit's Android 16 adaptation, cherry-picked onto libhybris master (all six
# applied cleanly). Needed because Android 16 blobs use ELF TLS relocations the
# q linker did not implement, and because hybris never calls
# linker_finalize_static_tls() - it enters through __loader_dlopen, not
# linker_main() - so every Android library with a TLS segment lands in static
# TLS. glibc's exit handlers then dlclose those libraries and the linker's
# CHECK(mod.static_offset == SIZE_MAX) aborts, which on a 16 GSI killed every
# getprop/setprop and with them start-android-hals.sh.
SRCREV = "032a289a5a55744b53caa008d4e4d84fdcc975d1"
PV = "0.1.0+git"
PR = "r3"
PE = "1"

SRC_URI = "git://github.com/Herrie82/libhybris;branch=herrie/android16-tls;protocol=https \
    file://0001-tests-build-test_audio-as-gnu99-for-strdup.patch \
    file://0002-linker-search-the-VNDK-APEX-when-there-is-no-ld.config.patch \
"

S = "${UNPACKDIR}/${BB_GIT_DEFAULT_DESTSUFFIX}/hybris"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# We need the android headers which are now provided for compatiblity reasons as external
# component. The android-headers are specific for the environment the android-system-image
# is build with and can differ between different machines.
DEPENDS += "virtual/android-headers wayland-native"

# Only MACHINEs which provide virtual/android-headers can build this
COMPATIBLE_MACHINE = "^halium$"

PROVIDES += "virtual/libgles1 virtual/libgles2 virtual/libgles3 virtual/egl virtual/mesa"

# most MACHINEs don't use libhybris and depend on mesa to provide *gl*
# Multiple .bb files are due to be built which each provide virtual/libgles1 (virtual/libgles2, virtual/egl)
EXCLUDE_FROM_WORLD = "1"

# We don't ship any android binaries but depend on someone else doing this
# Your image needs to pull right MACHINE specific implementation
# use VIRTUAL-RUNTIME_android-system-image in some packagegroup
# RDEPENDS cannot be used because this is TUNE_PKGARCH recipe and 
# android-system-image is MACHINE_ARCH
# RDEPENDS:${PN} += "${VIRTUAL-RUNTIME_android-system-image}"

EXTRA_OECONF = "--with-android-headers=${STAGING_INCDIR}/android"

# If you want to enable debugging/tracing functionality add the following to a bbappend
# EXTRA_OECONF += "--enable-debug --enable-trace"

# brokensep because configure creates include/android symlink to with-android-headers in ${S} not ${B}
inherit autotools-brokensep pkgconfig

# --with-android-headers makes configure set ANDROID_HEADERS_CFLAGS to -I$withval
# literally, and every .pc.in ends its Cflags with @ANDROID_HEADERS_CFLAGS@, so the
# absolute recipe-sysroot path is baked into the installed pkgconfig files. wrynose
# makes that fatal:
#   QA Issue: File /usr/lib/pkgconfig/libhardware.pc in package libhybris-dev
#   contains reference to TMPDIR [buildpaths]
# The headers install to ${includedir}/android, so point the .pc files there.
#
# Dropping --with-android-headers would take configure's other branch, which sets
# Requires: android-headers and needs no rewriting - but that branch also skips the
# AM_CONDITIONALs for the libnfc-nxp, hardware_legacy/wifi, hwcomposer2, gralloc1
# and vibrator headers, silently dropping those backends. Not worth it here.
do_install:append() {
    sed -i -e 's|${STAGING_INCDIR}/android|${includedir}/android|g' \
        ${D}${libdir}/pkgconfig/*.pc

    # Enlarge glibc's static TLS surplus for every service on the system.
    #
    # The q linker keeps the bionic DTV pointer in an initial-exec __thread
    # slot - it has to be initial-exec, because the TLSDESC resolvers cannot
    # do a C-level TLS access to find it. An initial-exec variable in a library
    # that arrives through dlopen can only be satisfied out of glibc's static
    # TLS surplus, and the default is small enough that anything reaching
    # libhybris behind a few other dlopens runs out:
    #
    #   Failed to open module module-droid-card.so:
    #   libhybris-common.so.1: cannot allocate memory in static TLS block
    #
    # This is not specific to one consumer. PulseAudio dies outright (no droid
    # card, so no master sink for module-remap-sink, and module-droid-hidl then
    # aborts the daemon from its own pa__done), while nyx-cmd merely reports
    # "module does not exist" for a module that is plainly installed - it uses
    # one error for both a missing file and a failed dlopen. Both are cured by
    # the same tunable, so set it once here, where the requirement originates,
    # rather than per unit.
    install -d ${D}${sysconfdir}/systemd/system.conf.d
    cat > ${D}${sysconfdir}/systemd/system.conf.d/10-hybris-static-tls.conf <<EOF
[Manager]
DefaultEnvironment=GLIBC_TUNABLES=glibc.rtld.optional_static_tls=8192
EOF
}

FILES:${PN} += "${sysconfdir}/systemd/system.conf.d/10-hybris-static-tls.conf"
