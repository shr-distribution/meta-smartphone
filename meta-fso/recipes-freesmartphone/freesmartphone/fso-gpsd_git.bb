DESCRIPTION = "freesmartphone.org gpsd compatibility daemon"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SECTION = "network"
DEPENDS = "dbus-glib"
SRCREV = "39e810899110a9bb302cf2064e1c0f73541fb4e6"
PV = "0.8+gitr${SRCPV}"
PE = "1"
PR = "r9"

SRC_URI = "\
  git://github.com/freesmartphone/fso-gpsd;protocol=https;branch=master \
  file://${PN} \
  file://${PN}.service \
"
S = "${WORKDIR}/git"

inherit autotools update-rc.d systemd

INITSCRIPT_NAME = "fso-gpsd-sysv"
INITSCRIPT_PARAMS = "defaults 35"

SYSTEMD_SERVICE_${PN} = "${PN}.service"
RPROVIDES_${PN} += "${PN}-systemd"
RREPLACES_${PN} += "${PN}-systemd"
RCONFLICTS_${PN} += "${PN}-systemd"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/fso-gpsd ${D}${sysconfdir}/init.d/fso-gpsd-sysv
    
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/${PN}.service ${D}${systemd_unitdir}/system
}

FILES_${PN} += "${sysconfdir}"
RDEPENDS_${PN} = "frameworkd"
RPROVIDES_${PN} = "gpsd"
# Disabled as we always got gpsd but not fso-gpsd in the fso-image. Needs
# fixing.
#RCONFLICTS_${PN} = "gpsd"
