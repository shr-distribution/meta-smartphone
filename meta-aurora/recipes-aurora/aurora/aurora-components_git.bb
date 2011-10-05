require aurora-base.inc

DESCRIPTION = "This are the QML components needed by the Aurora UI"
SRCREV = "${AURORA_SRCREV}"

DEPENDS = "qt4-x11-free"

PV = "0.1.0+gitr${SRCPV}"
PR = "${INC_PR}.1"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-components"

inherit autotools

EXTRA_OECONF_append = " --with-qt-basedir=qt4"

PACKAGES = "${PN}-dbg ${PN}"
FILES_${PN} = "${libdir}/qt4/imports/Aurora"
FILES_${PN}-dbg = "${libdir}/qt4/imports/Aurora/*/.debug"

