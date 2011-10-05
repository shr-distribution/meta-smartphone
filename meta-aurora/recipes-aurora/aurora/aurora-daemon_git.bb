require aurora-base.inc

DESCRIPTION = "This is the daemon responsible for the aurora user interface"
PR = "${INC_PR}.1"

DEPENDS = "qt4-x11-free"

PV = "0.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master file://aurora-daemon.init"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-daemon"

inherit autotools update-rc.d

EXTRA_OECONF_append = " --with-qt-basedir=qt4"

INITSCRIPT_NAME = "aurora-daemon"
INITSCRIPT_PARAMS = "defaults 90 10"

do_install_append() {
  install -d ${D}${sysconfdir}/init.d/
  install -m 0755 ${WORKDIR}/aurora-daemon.init ${D}${sysconfdir}/init.d/aurora-daemon
}
