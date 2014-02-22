DESCRIPTION = "A phone UI based on the freesmartphone.org framework"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SECTION = "x11"
DEPENDS = "python-efl ecore edje-native python-pyrex-native python-cython-native"
SRCREV = "f2b39bfe7d6e7e2288337f83f3a02f5541027ea8"
PV = "0.0.2+gitr${SRCPV}"
PE = "1"
PR = "r17"

SRC_URI = "${FREESMARTPHONE_GIT}/zhone.git;protocol=git;branch=master \
           file://0001-illume_kbd-Fix-compliation-with-new-efl-1.8.patch \
           file://80zhone.sh"
S = "${WORKDIR}/git"

inherit distutils

do_install_append() {
    install -d ${D}${sysconfdir}/X11/Xsession.d/
    install -m 0755 ${WORKDIR}/80zhone.sh ${D}${sysconfdir}/X11/Xsession.d/
}

FILES_${PN} += "${datadir} ${sysconfdir}"

RDEPENDS_${PN} = "\
  python-edbus \
  python-edje \
  python-ecore \
  python-logging \
  python-textutils \
  python-dbus \
  python-pycairo \
"
