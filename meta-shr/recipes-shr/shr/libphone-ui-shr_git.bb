DESCRIPTION = "SHR default module for the Phone UI daemon"
SECTION = "e/apps"
DEPENDS += " libphone-ui evas ecore edje edje-native elementary"
SRCREV = "4948ea3f6da9f457b5f4ec37c82b835d4c2c3fb9"
PE = "1"
PV = "0.0.1+gitr${SRCPV}"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "git://git.shr-project.org/repo/libphone-ui-shr.git;protocol=http;branch=master \
    file://0001-ui-utils-Replace-deprecated-elm_notify_orient_set.patch \
"

S = "${WORKDIR}/git"

inherit pkgconfig autotools-brokensep gettext

EXTRA_OECONF = "--with-edje-cc=${STAGING_BINDIR_NATIVE}/edje_cc"


do_configure_prepend() {
        autopoint --force
}

FILES_${PN} += "${libdir}/phoneui/modules/shr.so"
FILES_${PN}-dev += "${libdir}/phoneui/modules/*.la"
FILES_${PN}-dbg += "${libdir}/phoneui/modules/.debug"
FILES_${PN}-staticdev += "${libdir}/phoneui/modules/*.a"
