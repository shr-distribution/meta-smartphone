DESCRIPTION = "freesmartphone.org gpsd compatibility daemon"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SECTION = "network"
DEPENDS = "dbus-glib"
SRCREV = "39e810899110a9bb302cf2064e1c0f73541fb4e6"
PV = "0.8+gitr${SRCPV}"
PE = "1"
PR = "r3"

SRC_URI = "\
  ${FREESMARTPHONE_GIT}/fso-gpsd.git;protocol=git;branch=master \
  file://${PN} \
  file://${PN}.service \
"
S = "${WORKDIR}/git"

inherit autotools update-rc.d systemd

INITSCRIPT_NAME = "fso-gpsd"
INITSCRIPT_PARAMS = "defaults 35"

SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/fso-gpsd ${D}${sysconfdir}/init.d/
    install -d ${D}${base_libdir}/systemd/system/
    install -m 0644 ${WORKDIR}/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
}

FILES_${PN} += "${sysconfdir}"
RDEPENDS_${PN} = "frameworkd"
RPROVIDES_${PN} = "gpsd"
# Disabled as we always got gpsd but not fso-gpsd in the fso-image. Needs
# fixing.
#RCONFLICTS_${PN} = "gpsd"

PACKAGES =+ "${PN}-systemd"
FILES_${PN}-systemd += "${base_libdir}/systemd"
RDEPENDS_${PN}-systemd += "${PN}"
