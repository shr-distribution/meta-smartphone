DESCRIPTION = "intone is a mplayer frontend for openmoko phones"
HOMEPAGE = "http://code.google.com/p/intone/"
AUTHOR = "cchandel"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "files://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary eina sqlite3 dbus edbus libvorbis id3lib"
RDEPENDS_${PN} = "mplayer lame"

SRCREV = "cee8e6e65ca8ffa6cd284eb27f6dfae5906c232d"
PV = "0.70+gitr${SRCPV}"

SRC_URI = "git://code.google.com/p/intone;protocol=http \
file://0001-adapt-to-newer-elementary-API.patch \
file://0002-genlist-API-change-label_get-text_get.patch \
file://0003-fix-changed-callback-name.patch \
"

S = "${WORKDIR}/git"

inherit autotools

do_install_append() {
	mkdir -p "${D}/${datadir}/pixmaps"
	install -m 0644 "${S}/resources/intone.png" "${D}/${datadir}/pixmaps"
	mkdir -p "${D}/${datadir}/applications"
	install -m 0644 "${S}/resources/intone.desktop" "${D}/${datadir}/applications"
	mkdir -p "${D}/${datadir}/intone"
	for ico in "${S}/resources/"*.png; do
		if [ "$(basename $ico)" != "intone.png" ]; then
			install -m 0644 $ico "${D}/${datadir}/intone"
		fi
	done
}


FILES_${PN} += "/usr/share/intone/* /usr/share/applications/* /usr/share/pixmaps/*"

