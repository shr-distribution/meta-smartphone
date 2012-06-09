require ${BPN}.inc

PR = "${INC_PR}.0"

SRC_URI[md5sum] = "d192e790eaa5c8bd537ea980bdb37d95"
SRC_URI[sha256sum] = "91fe816d357377fc9824752c0f9a697db59f2f3d8d8766f8a435d66b6df02f12"

# The following adds support for the Palm Pre device which is not available in development
# anymore, so we need to specify it's configuration here. This can be removed with the
# next release 0.12 of fsogsmd.

EXTRA_OECONF_append = "--enable-modem-qualcomm-palm"
DEPENDS += " msmcommd-specs"

PACKAGES =+ "${PN}-module-lowlevel-palmpre ${PN}-module-lowlevel-palmpre-dev ${PN}-module-lowlevel-palmpre-dbg"
FILES_${PN}-module-lowlevel-palmpre = "${CORNUCOPIA_MODULE_DIR}/lowlevel_palmpre.so"
FILES_${PN}-module-lowlevel-palmpre-dev = "${CORNUCOPIA_MODULE_DIR}/lowlevel_palmpre.la"
FILES_${PN}-module-lowlevel-palmpre-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/lowlevel_palmpre*"

PACKAGES =+ "${PN}-module-modem-qualcomm-palm ${PN}-module-modem-qualcomm-palm-dev ${PN}-module-modem-qualcomm-palm-dbg"
FILES_${PN}-module-modem-qualcomm-palm = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_palm.so"
FILES_${PN}-module-modem-qualcomm-palm-dev = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_palm.la"
FILES_${PN}-module-modem-qualcomm-palm-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_qualcomm_palm*"

CONFFILES_${PN}-config += " ${sysconfdir}/freesmartphone/conf/palm_pre/fsogsmd.conf"
