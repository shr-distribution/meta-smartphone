DESCRIPTION = "SHR default module for the Phone UI daemon"
SECTION = "e/apps"
DEPENDS += " libphone-ui evas ecore edje edje-native elementary"
SRCREV = "1b693f573622dbdc9a51f01f084ec0884b22e208"
PE = "1"
PV = "0.0.1+gitr${SRCPV}"
PR = "r5"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fbd65380cdd255951079008b364516c"

SRC_URI = "git://git.shr-project.org/repo/libphone-ui-shr.git;protocol=http;branch=master \
           file://0001-adapt-to-API-change-in-r59160-s-homogenous-homogeneo.patch \
           file://0001-Replace-deprecated-elm_button_label_set-elm_label_la.patch \
           file://0001-message-new-view-addapt-to-elm-API-change-from-r6043.patch \
           file://0001-adapt-to-r60995-and-r60999-API-changes-for-anchorblo.patch \
           file://0001-adapt-to-r60802-API-changes-for-toggle.patch \
           file://0001-adapt-to-r60862-API-changes-for-slider.patch \
           file://0001-adapt-to-r60860-API-changes-for-hoversel.patch \
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
FILES_${PN}-static += "${libdir}/phoneui/modules/*.a"
