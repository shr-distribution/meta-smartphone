DESCRIPTION = "SHR default module for the Phone UI daemon"
SECTION = "e/apps"
DEPENDS += " libphone-ui evas ecore edje edje-native elementary"
SRCREV = "08b9614c6a65a9371e948d56b6e238eec36d8efb"
PE = "1"
PV = "0.0.1+gitr${SRCPV}"
PR = "r0"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "git://github.com/shr-distribution/libphone-ui-shr;protocol=https;branch=master \
"

S = "${WORKDIR}/git"

inherit pkgconfig autotools gettext

EXTRA_OECONF = "--with-edje-cc=${STAGING_BINDIR_NATIVE}/edje_cc"


do_configure_prepend() {
        autopoint --force
}

FILES_${PN} += "${libdir}/phoneui/modules/shr.so"
FILES_${PN}-dev += "${libdir}/phoneui/modules/*.la"
FILES_${PN}-dbg += "${libdir}/phoneui/modules/.debug"
FILES_${PN}-staticdev += "${libdir}/phoneui/modules/*.a"
