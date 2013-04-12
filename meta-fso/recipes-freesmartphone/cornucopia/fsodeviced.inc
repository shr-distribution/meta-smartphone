require cornucopia-base.inc

inherit fso-plugin

DEPENDS += "alsa-lib android-rpc libnl"
RPROVIDES_${PN} = "openmoko-alsa-scenarios virtual/alsa-scenarios"

EXTRA_OECONF = "\
  --enable-kernel26-rfkill \
  --disable-player-canberra \
  --enable-htcdream-powercontrol \
"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 27"

inherit systemd
SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "${PN}.service"
RPROVIDES_${PN} += "${PN}-systemd"

SRC_URI_append = " file://${PN}"
SRC_URI_append = " file://0001-fsodeviced-add-config-for-qemux86-and-qemux86-64-Aut.patch"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${S}/data/${PN}.service ${D}${systemd_unitdir}/system/${PN}.service
}

pkg_preinst_${PN}-config () {
    # work-arround for opkg complaining that it cannot replace alsa-default dir (leftover from older fsodeviced) with new alsa-default symlink
    rm -rf ${sysconfdir}/freesmartphone/conf/openmoko_gta/alsa-default/
}

# package modules with extra dependencies in extra packages RDEPENDed by config package

PACKAGES =+ "${PN}-config"
FILES_${PN}-config = "${sysconfdir}/freesmartphone/"
CONFFILES_${PN}-config = " \
  ${sysconfdir}/freesmartphone/conf/openmoko_gta/${PN}.conf \
  ${sysconfdir}/freesmartphone/conf/htc_qualcomm_dream/${PN}.conf \
  ${sysconfdir}/freesmartphone/conf/htc_qualcomm_msm/${PN}.conf \
  ${sysconfdir}/freesmartphone/conf/motorola_ezx/${PN}.conf \
"
RRECOMMENDS_${PN} += "${PN}-config"
RDEPENDS_${PN}-config += "${PN}-modules"

PACKAGES =+ "${PN}-module-accelerometer-kxsd9 ${PN}-module-accelerometer-kxsd9-dev ${PN}-module-accelerometer-kxsd9-dbg"
FILES_${PN}-module-accelerometer-kxsd9 = "${CORNUCOPIA_MODULE_DIR}/accelerometer_kxsd9.so"
FILES_${PN}-module-accelerometer-kxsd9-dev = "${CORNUCOPIA_MODULE_DIR}/accelerometer_kxsd9.la"
FILES_${PN}-module-accelerometer-kxsd9-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/accelerometer_kxsd9*"

PACKAGES =+ "${PN}-module-backlight-omappanel ${PN}-module-backlight-omappanel-dev ${PN}-module-backlight-omappanel-dbg"
FILES_${PN}-module-backlight-omappanel = "${CORNUCOPIA_MODULE_DIR}/backlight_omappanel.so"
FILES_${PN}-module-backlight-omappanel-dev = "${CORNUCOPIA_MODULE_DIR}/backlight_omappanel.la"
FILES_${PN}-module-backlight-omappanel-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/backlight_omappanel*"

PACKAGES =+ "${PN}-module-vibrator-omapvibe ${PN}-module-vibrator-omapvibe-dev ${PN}-module-vibrator-omapvibe-dbg"
FILES_${PN}-module-vibrator-omapvibe = "${CORNUCOPIA_MODULE_DIR}/vibrator_omapvibe.so"
FILES_${PN}-module-vibrator-omapvibe-dev = "${CORNUCOPIA_MODULE_DIR}/vibrator_omapvibe.la"
FILES_${PN}-module-vibrator-omapvibe-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/vibrator_omapvibe*"

PACKAGES =+ "${PN}-module-accelerometer-lis302 ${PN}-module-accelerometer-lis302-dev ${PN}-module-accelerometer-lis302-dbg"
FILES_${PN}-module-accelerometer-lis302 = "${CORNUCOPIA_MODULE_DIR}/accelerometer_lis302.so"
FILES_${PN}-module-accelerometer-lis302-dev = "${CORNUCOPIA_MODULE_DIR}/accelerometer_lis302.la"
FILES_${PN}-module-accelerometer-lis302-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/accelerometer_lis302*"

PACKAGES =+ "${PN}-module-openmoko-powercontrol ${PN}-module-openmoko-powercontrol-dev ${PN}-module-openmoko-powercontrol-dbg"
FILES_${PN}-module-openmoko-powercontrol = "${CORNUCOPIA_MODULE_DIR}/openmoko_powercontrol.so"
FILES_${PN}-module-openmoko-powercontrol-dev = "${CORNUCOPIA_MODULE_DIR}/openmoko_powercontrol.la"
FILES_${PN}-module-openmoko-powercontrol-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/openmoko_powercontrol*"

PACKAGES =+ "${PN}-module-n900-quirks ${PN}-module-n900-quirks-dev ${PN}-module-n900-quirks-dbg"
FILES_${PN}-module-n900-quirks = "${CORNUCOPIA_MODULE_DIR}/n900_quirks.so"
FILES_${PN}-module-n900-quirks-dev = "${CORNUCOPIA_MODULE_DIR}/n900_quirks.la"
FILES_${PN}-module-n900-quirks-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/n900_quirks*"

PACKAGES =+ "${PN}-module-router-qdsp5 ${PN}-module-router-qdsp5-dev ${PN}-module-router-qdsp5-dbg"
FILES_${PN}-module-router-qdsp5 = "${CORNUCOPIA_MODULE_DIR}/router_qdsp5.so"
FILES_${PN}-module-router-qdsp5-dev = "${CORNUCOPIA_MODULE_DIR}/router_qdsp5.la"
FILES_${PN}-module-router-qdsp5-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/router_qdsp5*"

PACKAGES =+ "${PN}-module-vibrator-timedoutputclass ${PN}-module-vibrator-timedoutputclass-dev ${PN}-module-vibrator-timedoutputclass-dbg"
FILES_${PN}-module-vibrator-timedoutputclass = "${CORNUCOPIA_MODULE_DIR}/vibrator_timedoutputclass.so"
FILES_${PN}-module-vibrator-timedoutputclass-dev = "${CORNUCOPIA_MODULE_DIR}/vibrator_timedoutputclass.la"
FILES_${PN}-module-vibrator-timedoutputclass-dbg = "${CORNUCOPIA_MODULE_DIR}/.debug/vibrator_timedoutputclass*"

## shared modules or modules without known OE machine to RDEPEND on them (so kept in main module for now)
#  accelerometer.so
#  audio.so
#  dummy_input.so
#  gpio_input.so
#  kernel26_cpufreq.so
#  kernel26_display.so
#  kernel26_firmwareloader.so
#  kernel26_leds.so
#  kernel26_powersupply.so
#  kernel26_rfkill.so
#  kernel26_rtc.so
#  kernel_idle.so
#  kernel_info.so
#  kernel_input.so
#  player_alsa.so
#  powercontrol_ifconfig.so
#  router_alsa.so
#  thinkpad_powercontrol.so
#  vibrator_ledclass.so
