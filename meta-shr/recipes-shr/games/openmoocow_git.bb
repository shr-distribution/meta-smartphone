DESCRIPTION = "OpenMooCow makes your phone (nearly) become a cow!"
HOMEPAGE = "http://www.srcf.ucam.org/~taw27/openmoko/openmoocow/"
AUTHOR = "Thomas White"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://COPYING;md5=44cce59bacf0a45692ad23d04b9d2b22"
SECTION = "applications/games"

DEPENDS = "gtk+ libsdl"

PV = "0.0.3+gitr${SRCPV}"

SRCREV = "39648419825cddfea1cb1321e552a12b71fede14"
SRC_URI = "git://git.bitwiz.org.uk/openmoocow.git;protocol=git;branch=master"

S = "${WORKDIR}/git"

inherit autotools
