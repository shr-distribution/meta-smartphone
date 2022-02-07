DESCRIPTION = "Utility for handling the gta04 GPS activation/deactivation"
HOMEPAGE = "http://www.freesmartphone.org"
AUTHOR = "Denis Carikli <GNUtoo@no-log.org>"
SECTION = "base"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

RDEPENDS_${PN} += "python-pygps python-pygobject python-core"

PR = "r3"
PV = "0.0.0+gitr${SRCPV}"

SRCREV = "0920464d6d8caa74abcb3dfacafd846ae5d0620b"

SRC_URI = "git://github.com/freesmartphone/utilities;protocol=https;branch=master \
           file://gps-handler.service \
           file://gps-handler \
"

S = "${WORKDIR}/git/gta04/gps-handler"

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "om-gta04"

inherit distutils update-rc.d systemd

INITSCRIPT_NAME = "gps-handler"
INITSCRIPT_PARAMS = "defaults 36 34"

SYSTEMD_SERVICE_${PN} = "gps-handler.service"
RPROVIDES_${PN} += "${PN}-systemd"
RREPLACES_${PN} += "${PN}-systemd"
RCONFLICTS_${PN} += "${PN}-systemd"

FILES_${PN} += "${sysconfdir}"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/gps-handler ${D}${sysconfdir}/init.d/
    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/gps-handler.service ${D}${systemd_unitdir}/system/gps-handler.service
}
