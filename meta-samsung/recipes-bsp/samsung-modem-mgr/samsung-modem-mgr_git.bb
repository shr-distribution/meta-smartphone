DESCRIPTION = "Samsung Modem Manager"
SECTION = "console"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a"
PV = "0.1.0+gitr${SRCPV}"
PR = "r1"

SRC_URI = " \
  git://github.com/morphis/samsung-modem-mgr.git;protocol=git;branch=master \
  file://samsung-modem-mgr.init"
S = "${WORKDIR}/git"

SRCREV = "307d749937a2a408f43e53720e2f3fda0b48fb15"

inherit autotools update-rc.d

INITSCRIPT_NAME = "samsung-modem-mgr"
INITSCRIPT_PARAMS = "defaults 20"

do_install_append() {
    install -d ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/samsung-modem-mgr.init ${D}${sysconfdir}/init.d/samsung-modem-mgr
}

PACKAGES += "${PN}-systemd"
FILES_${PN}-systemd = "${systemd_unitdir}/"
