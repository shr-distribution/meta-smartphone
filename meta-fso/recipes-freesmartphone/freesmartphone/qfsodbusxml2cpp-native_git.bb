DESCRIPTION = "FSO's variant of the qdbusxml2cpp utility"
SECTION = "devel"
DEPENDS = "qt4-native"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=cb8aedd3bced19bd8026d96a8b6876d7"
INC_PR = "r0"

SRCREV = "de705c0b633c612aedb1273340c36fae59be9511"

SRC_URI = "${FREESMARTPHONE_GIT}/qfsodbusxml2cpp.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

inherit autotools native
