DESCRIPTION = "A GObject Persistence Library"
AUTHOR = "Jürg Billeter, Michael 'Mickey' Lauer"
LICENSE = "LGPL-2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"
SECTION = "devel"
DEPENDS = "libgee"
SRCREV = "26180fd3c0fe4eb6abb7440f10e51d997719b97a"
PV = "0.1.0+gitr${SRCPV}"
PE = "1"
PR = "r1"

SRC_URI = "\
  ${FREESMARTPHONE_GIT}/${PN};protocol=git;branch=mickey \
"
S = "${WORKDIR}/git"

inherit autotools vala
