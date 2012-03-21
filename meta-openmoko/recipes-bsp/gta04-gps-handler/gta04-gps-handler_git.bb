DESCRIPTION = "Utility for handling the gta04 GPS activation/deactivation"
HOMEPAGE = "http://www.freesmartphone.org"
AUTHOR = "Denis Carikli <GNUtoo@no-log.org>"
SECTION = "base"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

RDEPENDS_${PN} += "python-pygps python-pygobject python-core"

PR = "r0"
PV = "0.0.0+gitr${SRCPV}"

SRCREV = "0920464d6d8caa74abcb3dfacafd846ae5d0620b"

SRC_URI = "${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master"

S = "${WORKDIR}/git/gta04/gps-handler"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit distutils
