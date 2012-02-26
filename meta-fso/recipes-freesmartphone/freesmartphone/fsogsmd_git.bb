require cornucopia.inc
inherit fso-plugin
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PR = "${INC_PR}.12"
PV = "0.5.0+gitr${SRCPV}"
PE = "1"

DEPENDS += "libfsosystem libfsotransport libfsoresource libgsm0710mux \
  libgisi ppp connman msmcomm-specs libsamsung-ipc fsodatad"

EXTRA_OECONF_append = "\
  --enable-libgsm0710mux \
  --enable-modem-nokia-isi \
  --enable-modem-qualcomm-palm \
  --enable-modem-samsung \
"

# package modules with extra dependencies in extra packages RDEPENDed by config package

PACKAGES =+ "${PN}-config"
FILES_${PN}-config = "${sysconfdir}/freesmartphone/"
CONFFILES_${PN}-config = " \
  ${sysconfdir}/freesmartphone/conf/openmoko_gta/fsogsmd.conf \
  ${sysconfdir}/freesmartphone/conf/palm_pre/fsogsmd.conf \
  ${sysconfdir}/freesmartphone/conf/htc_qualcomm_dream/fsogsmd.conf \
  ${sysconfdir}/freesmartphone/conf/htc_qualcomm_msm/fsogsmd.conf \
  ${sysconfdir}/freesmartphone/conf/GTA04/fsogsmd.conf \
"
RRECOMMENDS_${PN} += "${PN}-config"
RDEPENDS_${PN}-config += "${PN}-modules"

PACKAGES =+ "${PN}-connman ${PN}-connman-dev ${PN}-connman-dbg"
FILES_${PN}-connman = "${libdir}/connman/plugins/fsogsm.so"
FILES_${PN}-connman-dev = "${libdir}/connman/plugins/fsogsm.la"
FILES_${PN}-connman-dbg = "${libdir}/connman/plugins/.debug/fsogsm*"
RRECOMMENDS_${PN} += "${PN}-connman"

PACKAGES =+ "${PN}-module-lowlevel-palmpre ${PN}-module-lowlevel-palmpre-dev ${PN}-module-lowlevel-palmpre-dbg"
FILES_${PN}-module-lowlevel-palmpre = "${CORNUCOPIA_MODULE_DIR}/lowlevel_palmpre.so"
FILES_${PN}-module-lowlevel-palmpre-dev = "${CORNUCOPIA_MODULE_DIR}/lowlevel_palmpre.la"
FILES_${PN}-module-lowlevel-palmpre-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/lowlevel_palmpre*"

PACKAGES =+ "${PN}-module-modem-qualcomm-palm ${PN}-module-modem-qualcomm-palm-dev ${PN}-module-modem-qualcomm-palm-dbg"
FILES_${PN}-module-modem-qualcomm-palm = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_palm.so"
FILES_${PN}-module-modem-qualcomm-palm-dev = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_palm.la"
FILES_${PN}-module-modem-qualcomm-palm-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_qualcomm_palm*"

PACKAGES =+ "${PN}-module-modem-qualcomm-htc ${PN}-module-modem-qualcomm-htc-dev ${PN}-module-modem-qualcomm-htc-dbg"
FILES_${PN}-module-modem-qualcomm-htc = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_htc.so"
FILES_${PN}-module-modem-qualcomm-htc-dev = "${CORNUCOPIA_MODULE_DIR}/modem_qualcomm_htc.la"
FILES_${PN}-module-modem-qualcomm-htc-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_qualcomm_htc*"

PACKAGES =+ "${PN}-module-modem-nokia-isi ${PN}-module-modem-nokia-isi-dev ${PN}-module-modem-nokia-isi-dbg"
FILES_${PN}-module-modem-nokia-isi = "${CORNUCOPIA_MODULE_DIR}/modem_nokia_isi.so"
FILES_${PN}-module-modem-nokia-isi-dev = "${CORNUCOPIA_MODULE_DIR}/modem_nokia_isi.la"
FILES_${PN}-module-modem-nokia-isi-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_nokia_isi*"

PACKAGES =+ "${PN}-module-lowlevel-openmoko ${PN}-module-lowlevel-openmoko-dev ${PN}-module-lowlevel-openmoko-dbg"
FILES_${PN}-module-lowlevel-openmoko = "${CORNUCOPIA_MODULE_DIR}/lowlevel_openmoko.so"
FILES_${PN}-module-lowlevel-openmoko-dev = "${CORNUCOPIA_MODULE_DIR}/lowlevel_openmoko.la"
FILES_${PN}-module-lowlevel-openmoko-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/lowlevel_openmoko*"

PACKAGES =+ "${PN}-module-modem-ti-calypso ${PN}-module-modem-ti-calypso-dev ${PN}-module-modem-ti-calypso-dbg"
FILES_${PN}-module-modem-ti-calypso = "${CORNUCOPIA_MODULE_DIR}/modem_ti_calypso.so"
FILES_${PN}-module-modem-ti-calypso-dev = "${CORNUCOPIA_MODULE_DIR}/modem_ti_calypso.la"
FILES_${PN}-module-modem-ti-calypso-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_ti_calypso*"

PACKAGES =+ "${PN}-module-modem-freescale-neptune ${PN}-module-modem-freescale-neptune-dev ${PN}-module-modem-freescale-neptune-dbg"
FILES_${PN}-module-modem-freescale-neptune = "${CORNUCOPIA_MODULE_DIR}/modem_freescale_neptune.so"
FILES_${PN}-module-modem-freescale-neptune-dev = "${CORNUCOPIA_MODULE_DIR}/modem_freescale_neptune.la"
FILES_${PN}-module-modem-freescale-neptune-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_freescale_neptune*"

PACKAGES =+ "${PN}-module-lowlevel-motorola-ezx ${PN}-module-lowlevel-motorola-ezx-dev ${PN}-module-lowlevel-motorola-ezx-dbg"
FILES_${PN}-module-lowlevel-motorola-ezx = "${CORNUCOPIA_MODULE_DIR}/lowlevel_motorola_ezx.so"
FILES_${PN}-module-lowlevel-motorola-ezx-dev = "${CORNUCOPIA_MODULE_DIR}/lowlevel_motorola_ezx.la"
FILES_${PN}-module-lowlevel-motorola-ezx-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/lowlevel_motorola_ezx*"

PACKAGES =+ "${PN}-module-lowlevel-samsung-crespo ${PN}-module-lowlevel-samsung-crespo-dev ${PN}-module-lowlevel-samsung-crespo-dbg"
FILES_${PN}-module-lowlevel-samsung-crespo = "${CORNUCOPIA_MODULE_DIR}/lowlevel_samsung_crespo.so"
FILES_${PN}-module-lowlevel-samsung-crespo-dev = "${CORNUCOPIA_MODULE_DIR}/lowlevel_samsung_crespo.la"
FILES_${PN}-module-lowlevel-samsung-crespo-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/lowlevel_samsung_crespo*"

PACKAGES =+ "${PN}-module-modem-samsung ${PN}-module-modem-samsung-dev ${PN}-module-modem-samsung-dbg"
FILES_${PN}-module-modem-samsung = "${CORNUCOPIA_MODULE_DIR}/modem_samsung.so"
FILES_${PN}-module-modem-samsung-dev = "${CORNUCOPIA_MODULE_DIR}/modem_samsung.la"
FILES_${PN}-module-modem-samsung-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_samsung*"

PACKAGES =+ "${PN}-module-modem-option-gtm601 ${PN}-module-modem-option-gtm601-dev ${PN}-module-modem-option-gtm601-dbg"
FILES_${PN}-module-modem-option-gtm601 = "${CORNUCOPIA_MODULE_DIR}/modem_option_gtm601.so"
FILES_${PN}-module-modem-option-gtm601-dev = "${CORNUCOPIA_MODULE_DIR}/modem_option_gtm601.la"
FILES_${PN}-module-modem-option-gtm601-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/modem_option_gtm601*"

## shared modules or modules without known OE machine to RDEPEND on them (so kept in main module for now)
#  dbus_service.so
#  modem_dummy.so
#  modem_cinterion_mc75.so
#  modem_singleline.so
#  pdp_ppp.so
#  pdp_ppp_internal.so
#  pdp_ppp_mux.so
#  pdp_qmi.so
#  ppp2fsogsmd.so
