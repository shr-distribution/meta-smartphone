FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_nokia900 = " file://0001-video-out-for-omapfb-support.patch"
# We want a kernel header for nokia900, but we don't want to make mplayer machine specific for that
STAGING_KERNEL_DIR = "${STAGING_DIR}/${MACHINE_ARCH}${TARGET_VENDOR}-${TARGET_OS}/kernel"

EXTRA_OECONF_append_nokia900 = " --enable-armv6 --enable-neon"

# drop FULL_OPTIMIZATION for now, because gcc-4.6 got ICE with -O4 and -ffast-math now like in libvpx
# http://lists.linuxtogo.org/pipermail/openembedded-devel/2011-November/036001.html
# but lowering to -O3 is not enough in this case
# FULL_OPTIMIZATION = "-fexpensive-optimizations -fomit-frame-pointer -frename-registers -O4 -ffast-math"
# FULL_OPTIMIZATION_nokia900 = "-fexpensive-optimizations -fomit-frame-pointer -ftree-vectorize -O4 -ffast-math"

do_configure_prepend_nokia900() {
        cp ${STAGING_KERNEL_DIR}/arch/arm/plat-omap/include/mach/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_KERNEL_DIR}/include/asm-arm/arch-omap/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_KERNEL_DIR}/include/linux/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_DIR_TARGET}/kernel/include/linux/omapfb.h ${S}/libvo/omapfb.h || true
        sed -e 's/__user//g' -i ${S}/libvo/omapfb.h || true
}
