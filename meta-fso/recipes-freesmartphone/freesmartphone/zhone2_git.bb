DESCRIPTION = "A demo phone application based on FSO 2.0"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
HOMEPAGE = "http://www.freesmartphone.org"
SECTION = "x11"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
DEPENDS = "edje-native evas ecore edje libeflvala libfsobasics libfsoframework libfso-glib"
SRCREV = "fd8c57d835d0ce6db12dae18c3d2fdda0b254de8"
PV = "2.0.0+gitr${SRCPV}"
PR = "r3"
PE = "1"

SRC_URI = "\
  ${FREESMARTPHONE_GIT}/aurora.git;protocol=git;branch=master \
  http://www.linuxtogo.org/~mickeyl/misc/splash;name=splash \
  file://zhone2 \
"
SRC_URI[splash.md5sum] = "e198d5c83cc47a216562e435df0e03c3"
SRC_URI[splash.sha256sum] = "3e41bcf4ea50ab7406d3833e412a78ddb9afaab4b0a8cca1bf93755d1ce9a215"
S = "${WORKDIR}/git/zhone2"

inherit autotools update-rc.d vala

INITSCRIPT_NAME = "zhone2"
INITSCRIPT_PARAMS = "defaults 40"

do_install_append() {
	install -d ${D}${datadir}/zhone2/
	install -d ${D}${sysconfdir}/init.d/
	install -m 0644 ${WORKDIR}/splash ${D}${datadir}/zhone2/
	install -m 0755 ${WORKDIR}/${INITSCRIPT_NAME} ${D}${sysconfdir}/init.d/
}
