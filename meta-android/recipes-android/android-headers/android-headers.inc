SUMMARY = "Android header files for machine ${MACHINE}"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

PROVIDES += "virtual/android-headers"

do_install() {
    install -d ${D}${includedir}/android
    cp -rv ${S}/* ${D}${includedir}/android/
    rm ${D}${includedir}/android/android-headers.pc

    if [ -e ${S}/android-config_${MACHINE}.h ] ; then
        rm ${D}${includedir}/android/android-config.h
        install ${S}/android-config_${MACHINE}.h ${D}${includedir}/android-config.h
    fi

    install -d ${D}${libdir}/pkgconfig
    install -m 0644 ${S}/android-headers.pc ${D}${libdir}/pkgconfig
}

FILES_${PN}-dev += "${libdir}/pkg-config"
