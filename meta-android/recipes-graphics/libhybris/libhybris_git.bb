DESCRIPTION = "Hybris is a solution that commits hybris, by allowing us to use \
bionic-based HW adaptations in glibc systems"
# FIXME license of some parts is Apache-2.0 but not all parts clearly states which license
# is used for them.
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRCREV = "fd3797991ff4dba37b530269c5524c013a6fbaf5"
PV = "0.0.0+gitr${SRCPV}"
PR = "r0"

SRC_URI = "git://github.com/morphis/libhybris;branch=master;protocol=git"
S = "${WORKDIR}/git"

do_compile() {
    export PARALLEL_MAKE=""
    oe_runmake ARCH=arm
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/test_egl ${D}${bindir}
    install -m 0755 ${S}/test_glesv2 ${D}${bindir}
    install -m 0755 ${S}/test_gingerbread ${D}${bindir}

    install -d ${D}${libdir}
    oe_libinstall -C ${S} -so libEGL ${D}${libdir}
    oe_libinstall -C ${S} -so libGLESv2 ${D}${libdir}
    oe_libinstall -C ${S} -so libhybris_gingerbread ${D}${libdir}

    install -d ${D}${includedir}
    cp -ra ${S}/include/* ${D}${includedir}/
}
