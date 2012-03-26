require aurora-base.inc

DESCRIPTION = "This is the daemon responsible for the aurora user interface"

LICENSE = "GPLv2 & Other"
LIC_FILES_CHKSUM = " \
  file://COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe \
  file://COPYING;md5=2c195aa863d9e4c30a30752d48fe8f51 \
"

PR = "${INC_PR}.14"

DEPENDS = "qt4-x11-free libfsobasics libfsoframework dconf-qt"
RDEPENDS_${PN} = "qt4-plugin-imageformat-svg"

PV = "0.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master"
SRCREV = "${AURORA_SRCREV}"
S = "${WORKDIR}/git/aurora-daemon"

inherit autotools update-alternatives

export XDG_DATA_DIRS = "${STAGING_DATADIR}"
EXTRA_OECONF_append = " --with-qt-basedir=qt4"

FILES_${PN} += " ${datadir}/aurora ${libdir}/qt4/imports/Aurora ${datadir}/aurora \
  ${datadir}/glib-2.0/schemas/org.aurora.gschema.xml"
FILES_${PN}-dbg += "${libdir}/qt4/imports/Aurora/*/.debug ${datadir}/aurora/applications/*/.debug"

ALTERNATIVE_PATH = "${bindir}/aurora-daemon"
ALTERNATIVE_NAME = "x-window-manager"
ALTERNATIVE_LINK = "${bindir}/x-window-manager"
ALTERNATIVE_PRIORITY = "16"
