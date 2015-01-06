DESCRIPTION = "libGLES for OMAP4 based devices"
LICENSE = "TI-TSPA"
LIC_FILES_CHKSUM = "file://license.txt;md5=542af8025bb26f9b36a120c00e319748"

SRC_URI = "https://launchpad.net/ubuntu/+archive/primary/+files/pvr-omap4_${PV}.orig.tar.gz"
S = "${WORKDIR}/pvr-omap4-${PV}"

SRC_URI[md5sum] = "4ab152564f6bd98c0afe5ed0dcb9b72a"
SRC_URI[sha256sum] = "c65b6af74d186bffedc9750c65db8f525df932e5df23836cf58cd4d02e5b871d"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r3"
PROVIDES += "virtual/libgl virtual/libgles1 virtual/libgles2 virtual/egl"
DEPENDS = "libdrm"

do_install() {
    # install all necessary libraries
    # NOTE: insane.bbclass will warn about redundant RAPTH's in the libraries
    install -d ${D}${libdir}
    install -d ${D}${libdir}/xorg/modules/drivers
    install -d ${D}${libdir}/.debug

    cp -pPR ${S}/usr/lib/*.so* ${D}${libdir}
    cp -pPR ${S}/usr/lib/xorg/modules/drivers/* ${D}${libdir}/xorg/modules/drivers/
    cp -pPR ${S}/usr/lib/debug/usr/lib/*.so* ${D}${libdir}/.debug

    # install all ncessary development files
    install -d ${D}${libdir}/pkgconfig
    install -d ${D}${includedir}/GLES
    install -d ${D}${includedir}/KHR
    install -d ${D}${includedir}/VG
    install -d ${D}${includedir}/EGL
    install -d ${D}${includedir}/GLES2

    cp -pR ${S}/usr/include/GLES/*.h ${D}${includedir}/GLES/
    cp -pR ${S}/usr/include/KHR/*.h ${D}${includedir}/KHR/
    cp -pR ${S}/usr/include/VG/*.h ${D}${includedir}/VG/
    cp -pR ${S}/usr/include/EGL/*.h ${D}${includedir}/EGL/
    cp -pR ${S}/usr/include/GLES2/*.h ${D}${includedir}/GLES2/
    cp -pR ${S}/usr/lib/pkgconfig/* ${D}${libdir}/pkgconfig/

    # install different utilities
    install -d ${D}${bindir}

    install -m 0755 ${S}/usr/bin/xgles2test1 ${D}${bindir}
    install -m 0755 ${S}/usr/bin/xovg_unit_test ${D}${bindir}
    install -m 0755 ${S}/usr/bin/xeglinfo ${D}${bindir}
    install -m 0755 ${S}/usr/bin/xgles1test1 ${D}${bindir}
    install -m 0755 ${S}/usr/bin/xmultiegltest ${D}${bindir}

    # some data files needed for the tests
    install -d ${D}${datadir}/sgx-lib
    cp ${S}/usr/share/sgx-lib/* ${D}${datadir}/sgx-lib/
}

PACKAGES += " ${PN}-test"
FILES_${PN}-test = " \
  ${bindir}/xgles2test1 \
  ${bindir}/xovg_unit_test \
  ${bindir}/xeglinfo \
  ${bindir}/xgles1test1 \
  ${bindir}/xmultiegltest \
  ${datadir}/sgx-lib/"
