DESCRIPTION = "Vala application for monitoring raw sensor values on the handheld"
AUTHOR = "Michele Brocco <ssj2micvm@gmail.com>"
LICENSE = "GPL-3.0"
# COPYING is symlink to /usr/share/automake-1.11/COPYING and wont work on host with different automake version
LIC_FILES_CHKSUM = "file://src/main.vala;endline=20;md5=219b04c881db0b80d198021b04d17c8a"
DEPENDS = "glib-2.0 gtk+ libgee cairo libsensmon"
RDEPENDS_${PN} = "glib-2.0 gtk+ libgee cairo"
RSUGGESTS_${PN} = ""
PV = "0.21+gitr${SRCPV}"
PR = "r1"

PNBLACKLIST[sensmon] = "Doesn't build after vala upgrade from 0.24.0 to 0.26.1"

SRC_URI = "git://gitorious.org/sensor-monitor/sensor-monitor.git;protocol=git;branch=master"

SRCREV = "bb2f8dfd5615abec96cc3e19a354e24a22991a4c"
S = "${WORKDIR}/git"

inherit autotools vala pkgconfig

FILES_${PN} += "${datadir}/${PN} ${datadir}/applications ${datadir}/pixmaps"

MAINTAINER = "Michele Brocco <ssj2micvm@gmail.com>"
