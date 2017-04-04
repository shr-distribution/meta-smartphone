DESCRIPTION = "Compass written in vala that uses the hmc5843 compass and the lis302dl accelerometer"
AUTHOR = "Michele Brocco <ssj2micvm@gmail.com>"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
DEPENDS = "glib-2.0 gtk+ libgee cairo libsensmon"
RDEPENDS_${PN} = "glib-2.0 gtk+ libgee cairo"
RSUGGESTS_${PN} = ""
PV = "0.11+gitr${SRCPV}"
PR = "r1"

PNBLACKLIST[valacompass] = "Doesn't build after vala upgrade from 0.24.0 to 0.26.1 - the recipe will be removed on 2017-09-01 unless the issue is fixed"

SRC_URI = "git://gitorious.org/vala-compass/vala-compass.git;protocol=git;branch=master"

SRCREV = "9ca9b568a055ff2412f0d642987421178334e683"
S = "${WORKDIR}/git"

inherit autotools vala pkgconfig

FILES_${PN} += "${datadir}/${PN} ${datadir}/applications ${datadir}/pixmaps"

MAINTAINER = "Michele Brocco <ssj2micvm@gmail.com>"
