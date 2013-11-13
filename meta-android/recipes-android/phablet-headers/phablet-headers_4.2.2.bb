SUMMARY = "Android header files of phablet environment (http://phablet.ubuntu.com/gitweb)"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

SRC_URI = "git://github.com/webOS-ports/phablet-headers.git;branch=master;protocol=git"
SRCREV = "cfbcae588a1c90fa57fbe4ad44af791c665fafd4"
S = "${WORKDIR}/git"

PROVIDES += "virtual/android-headers"

do_install() {
    install -d ${D}${includedir}/android
    cp -rv ${S}/* ${D}${includedir}/android/
    rm ${D}${includedir}/android/android-headers.pc

    install -d ${D}${libdir}/pkgconfig
    install -m 0644 ${S}/android-headers.pc ${D}${libdir}/pkgconfig
}

FILES_${PN}-dev += "${libdir}/pkg-config"
