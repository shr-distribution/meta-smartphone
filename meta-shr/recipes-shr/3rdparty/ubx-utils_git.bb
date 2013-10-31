DESCRIPTION = "Various Utilities for UBX GPS"
AUTHOR = "Timo Juhani Lindfors <timo.lindfors@iki.fi>"
SECTION = "base"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=838c366f69b72c5df05c96dff79b35f2"

RDEPENDS_${PN} += "python-core"

PV = "0.0.0+gitr${SRCPV}"

SRCREV = "b63c0932ddfe0d6c6ee9b74553b175e9f2c6c4be"

SRC_URI = "git://lindi.iki.fi/lindi/git/ubx.git;protocol=http;branch=master"

S = "${WORKDIR}/git"

inherit distutils
