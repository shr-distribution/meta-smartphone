DESCRIPTION = "intone is a mplayer frontend for openmoko phones"
HOMEPAGE = "http://code.google.com/p/intone/"
AUTHOR = "cchandel"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "files://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary eina sqlite3 dbus edbus libvorbis id3lib"
RDEPENDS_${PN} = "mplayer lame"

SRCREV = "d5c211f7f5ed815437b4dbd880bf2fa3af7105e1"
PV = "0.70+gitr${SRCPV}"
PR = "r1"

SRC_URI = "git://code.google.com/p/intone;protocol=http \
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

