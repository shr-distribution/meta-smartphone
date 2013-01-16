DESCRIPTION = "Samsung RFS Manager"
SECTION = "console"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a"
PV = "0.1.0+gitr${SRCPV}"
PR = "r1"

DEPENDS = "glib-2.0"

SRC_URI = " \
  git://github.com/morphis/samsung-rfs-mgr.git;protocol=git;branch=master \
  file://samsung-rfs-mgr.init"
S = "${WORKDIR}/git"

SRCREV = "db81eaaed9b8c1c7e4e1998837d10282bb59b5b6"

inherit autotools update-rc.d

INITSCRIPT_NAME = "samsung-rfs-mgr"
INITSCRIPT_PARAMS = "defaults 20"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/samsung-rfs-mgr.init ${D}${sysconfdir}/init.d/samsung-rfs-mgr
}

PACKAGES += "${PN}-systemd"
FILES_${PN}-systemd = "${systemd_unitdir}/"
