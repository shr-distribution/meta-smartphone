DESCRIPTION = "Samsung RFS Manager"
SECTION = "console"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a"
PV = "0.1.0+gitr${SRCPV}"
PR = "r2"

DEPENDS = "glib-2.0 libsamsung-ipc"

# depends on libsamsung-ipc_git which has negative D_P
# Requested 'samsung-ipc-1.0 >= 0.2' but version of libsamsung-ipc is 0.1.0
EXCLUDE_FROM_WORLD = "1"

SRC_URI = " \
  git://github.com/morphis/samsung-rfs-mgr.git;protocol=git;branch=master \
  file://samsung-rfs-mgr.init"
S = "${WORKDIR}/git"

SRCREV = "db81eaaed9b8c1c7e4e1998837d10282bb59b5b6"

inherit autotools update-rc.d systemd

INITSCRIPT_NAME = "samsung-rfs-mgr"
INITSCRIPT_PARAMS = "defaults 20"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/samsung-rfs-mgr.init ${D}${sysconfdir}/init.d/samsung-rfs-mgr
}

SYSTEMD_SERVICE_${PN} = "samsung-rfs-mgr.service"
RPROVIDES_${PN} += "${PN}-systemd"
RREPLACES_${PN} += "${PN}-systemd"
RCONFLICTS_${PN} += "${PN}-systemd"
