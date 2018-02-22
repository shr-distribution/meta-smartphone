SUMMARY = "Android header files for machine ${MACHINE}"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PACKAGE_ARCH = "${MACHINE_ARCH}"

ANDROID_API ?= "19"
PROVIDES += "virtual/android-headers"

do_install() {
    install -d ${D}${includedir}/android
    cp -rv ${S}/git/* ${D}${includedir}/android/

    if [ -n "${ANDROID_HEADERS_DEFINES}" ] ; then
        rm ${D}${includedir}/android/android-config.h
        sed '/\/\* CONFIG GOES HERE/,$d' ${S}/git/android-config.h > android-config.h.pre
        sed '0,/\/\* CONFIG GOES HERE/d' ${S}/git/android-config.h > android-config.h.post
        for i in ${ANDROID_HEADERS_DEFINES}; do
          echo "#define $i 1" >> android-config_${MACHINE}.h
        done
        cat android-config.h.pre android-config_${MACHINE}.h android-config.h.post >> android-config.h.new
        install android-config.h.new ${D}${includedir}/android/android-config.h
    fi

    install -d ${D}${libdir}/pkgconfig
    install -m 0644 ${S}/git/android-headers.pc ${D}${libdir}/pkgconfig/android-headers.pc
    ln -s android ${D}${includedir}/android-${ANDROID_API}
}

FILES_${PN}-dev += "${libdir}/pkg-config"
