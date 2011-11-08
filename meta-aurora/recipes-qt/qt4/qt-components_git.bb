DESCRIPTION = "Qt Quick Components project"
AUTHOR = "Nokia Corporation and/or its subsidiary(-ies)"
HOMEPAGE = "http://qt.nokia.com/"
SECTION = "qt4"

LICENSE = "BSD & GFDL-1.3"
LIC_FILES_CHKSUM = " \
  file://header.BSD;md5=6127ef7232170f61b7c5f4da50768c27 \
  file://LICENSE.FDL;md5=6d9f2a9af4c8b8c3c769f6cc1b6aaf7e \
"

PV = "1.2+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://git.freesmartphone.org/qt-components.git;protocol=git;branch=aurora-support"
S = "${WORKDIR}/git"
SRCREV = "e03fdfc8cf92afbed83a6cc0c6e9ef56d1617b1f"

inherit qt4x11

do_configure() {
  ./configure -prefix /usr -symbian -nomake tests -no-mobility -qmake-exec ${STAGING_BINDIR_NATIVE}/qmake2
  # remove host lib search path from makefiles
  for mf in `find ${S} -name Makefile`; do
    sed -i -e 's:-L${libdir}::g' \
           -e 's:-I${includedir}/QtMobility::g' \
           -e 's:-I${includedir}::g' $mf

  done
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

