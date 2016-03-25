DESCRIPTION = "Samsung Modem Manager"
SECTION = "console"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a"
PV = "0.1.0+gitr${SRCPV}"
PR = "r3"

DEPENDS = "dbus glib-2.0 libsamsung-ipc"

# needs newer samsung-ipc which has negative D_P, 
# Requested 'samsung-ipc-1.0 >= 0.2' but version of libsamsung-ipc is 0.1.0
EXCLUDE_FROM_WORLD = "1"

SRC_URI = " \
  git://github.com/morphis/samsung-modem-mgr.git;protocol=git;branch=master \
  file://samsung-modem-mgr.init"
S = "${WORKDIR}/git"

SRCREV = "4cf06005d676855285c88774b4ce96ed22abac0d"

inherit autotools update-rc.d systemd pkgconfig

INITSCRIPT_NAME = "samsung-modem-mgr"
INITSCRIPT_PARAMS = "defaults 20"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/samsung-modem-mgr.init ${D}${sysconfdir}/init.d/samsung-modem-mgr
}

SYSTEMD_SERVICE_${PN} = "samsung-modem-mgr.service"
RPROVIDES_${PN} += "${PN}-systemd"
RREPLACES_${PN} += "${PN}-systemd"
RCONFLICTS_${PN} += "${PN}-systemd"
