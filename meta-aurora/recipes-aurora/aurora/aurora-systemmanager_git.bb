require aurora-base.inc

DESCRIPTION = "This is the system manager for a system powered by the Aurora UI"
SRCREV = "${AURORA_SRCREV}"
DEPENDS = "libfsobasics libfsoframework libfsosystem libfso-glib"

PV = "0.1.0+gitr${SRCPV}"
PR = "${INC_PR}.0"

SRC_URI = " \
  ${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master \
  file://aurora-systemmanager \
"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-systemmanager"

INITSCRIPT_NAME = "aurora-systemmanager"
INITSCRIPT_PARAMS = "defaults 85 10"

inherit autotools update-rc.d vala

do_install_append() {
  install -d ${D}${sysconfdir}/init.d/
  install -m 0755 ${WORKDIR}/aurora-systemmanager ${D}${sysconfdir}/init.d/
}

FILES_${PN} += "${sysconfdir}/aurora-systemmanager.conf"
CONFFILES_${PN} = "${sysconfdir}/aurora-systemmanager.conf"
FILES_${PN}-dbg += " ${sbindir}/.debug/aurora-systemmanager"
