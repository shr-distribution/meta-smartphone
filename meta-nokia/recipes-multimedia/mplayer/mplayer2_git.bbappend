FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

SRC_URI_append_nokia900 = " file://0001-video-out-for-omapfb-support.patch"
# We want a kernel header for nokia900, but we don't want to make mplayer machine specific for that
STAGING_KERNEL_DIR = "${STAGING_DIR}/${MACHINE_ARCH}${TARGET_VENDOR}-${TARGET_OS}/kernel"

do_configure_prepend_nokia900() {
        cp ${STAGING_KERNEL_DIR}/arch/arm/plat-omap/include/mach/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_KERNEL_DIR}/include/asm-arm/arch-omap/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_KERNEL_DIR}/include/linux/omapfb.h ${S}/libvo/omapfb.h || true
        cp ${STAGING_DIR_TARGET}/kernel/include/linux/omapfb.h ${S}/libvo/omapfb.h || true
        sed -e 's/__user//g' -i ${S}/libvo/omapfb.h || true
}
