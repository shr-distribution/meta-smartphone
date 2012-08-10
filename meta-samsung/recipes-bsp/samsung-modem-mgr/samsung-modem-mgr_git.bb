DESCRIPTION = "Samsung Modem Manager"
SECTION = "console"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=eb723b61539feef013de476e68b5c50a"
PV = "0.1.0+gitr${SRCPV}"
PR = "r0"

SRC_URI = "git://github.com/morphis/samsung-modem-mgr.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

SRCREV = "307d749937a2a408f43e53720e2f3fda0b48fb15"

inherit autotools

PACKAGES += "${PN}-systemd"
FILES_${PN}-systemd = "${systemd_unitdir}/"
