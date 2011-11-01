DESCRIPTION = "Qt Quick Components project - ready make QML components"
AUTHOR = "Nokia Corporation and/or its subsidiary(-ies)"
HOMEPAGE = "http://qt.nokia.com/"
SECTION = "qt4"

LICENSE = "BSD & FDL"
LIC_FILES_CHKSUM = " \
  file://header.BSD;md5=6127ef7232170f61b7c5f4da50768c27 \
  file://LICENSE.FDL;md5=6d9f2a9af4c8b8c3c769f6cc1b6aaf7e \
"

PV = "1.2+gitr${SRCPV}"
PR = "r0"

SRC_URI = "git://git.freesmartphone.org/qt-components.git;protocol=git;branch=aurora-support"
S = "${WORKDIR}/git"
SRCREV = "cde21582b0619b9bf8d9183503e4e17424f603d1"

inherit qt4x11

do_configure() {
  ./configure -prefix /usr -symbian -nomake tests -no-mobility
}

do_install() {
  oe_runmake install INSTALL_ROOT=${D}
}

FILES_${PN}-dbg += " \
  ${libdir}/qt4/imports/Qt/labs/components.1.1/.debug \
  ${libdir}/qt4/imports/Qt/labs/components/native/.debug \
  ${libdir}/qt4/imports/com/nokia/symbian.1.1/.debug \
  ${libdir}/qt4/imports/com/nokia/extras.1.1/.debug \
"
FILES_${PN} += "${libdir}/qt4/imports"

