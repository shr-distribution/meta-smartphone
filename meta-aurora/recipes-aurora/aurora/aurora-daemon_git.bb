require aurora-base.inc

DESCRIPTION = "This is the daemon responsible for the aurora user interface"
PR = "${INC_PR}.0"

DEPENDS = " \
  qt4-embedded \
"

PV = "0.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master file://aurora-daemon.init"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-daemon"

inherit autotools update-rc.d

EXTRA_OECONF_append = " \
  --with-qt-basedir=qtopia \
  --enable-qws-support \
"

INITSCRIPT_NAME = "aurora-daemon"
INITSCRIPT_PARAMS = "defaults 90 10"

do_install_append() {
  install -d ${D}${sysconfdir}/init.d/
  install -m 0755 ${WORKDIR}/aurora-daemon.init ${D}${sysconfdir}/init.d/aurora-daemon
}

do_install_append_palmpre() {
  mkdir -p ${D}${sysconfdir}/default
  echo "AURORA_EXTRA_ARGS=\"-display directfb\"" > ${D}${sysconfdir}/default/aurora
}
