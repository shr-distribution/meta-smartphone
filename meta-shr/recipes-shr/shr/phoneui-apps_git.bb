DESCRIPTION = "The PhoneUI app starters"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"
SECTION = "x11/applications"
DEPENDS += "dbus-glib"
SRCREV = "195a3726c121939d921b1417c0512a96a24c381d"
PV = "0.0.0+gitr${SRCPV}"
PR = "r1"

inherit pkgconfig autotools

SRC_URI = "git://git.shr-project.org/repo/phoneui-apps.git;protocol=http;branch=master"
S = "${WORKDIR}/git"

PACKAGES =+ "${PN}-dialer ${PN}-messages ${PN}-contacts ${PN}-quick-settings ${PN}-phone-log"

FILES_${PN}-dialer = "\
	${bindir}/phoneui-dialer \
	${datadir}/applications/phoneui-dialer.desktop \
"
FILES_${PN}-messages = "\
	${bindir}/phoneui-messages \
	${datadir}/applications/phoneui-messages.desktop \
"
FILES_${PN}-contacts = "\
	${bindir}/phoneui-contacts \
	${datadir}/applications/phoneui-contacts.desktop \
"
FILES_${PN}-phone-log = "\
	${bindir}/phoneui-phone-log \
	${datadir}/applications/phoneui-phone-log.desktop \
"
FILES_${PN}-quick-settings = "\
	${bindir}/phoneui-quick-settings \
"
FILES_${PN}-dbg += "\
	${bindir}/.debug \
"
