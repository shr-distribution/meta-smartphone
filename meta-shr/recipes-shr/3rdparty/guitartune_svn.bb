DESCRIPTION = "guitar tuner for openmoko phones"
HOMEPAGE = "http://code.google.com/p/guitartune"
AUTHOR = "cchandel"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
SECTION = "e/apps"
DEPENDS = "gtk+ libglade fftw sqlite3"

SRCREV = "11"
PV = "0.36+svnr${SRCPV}"

SRC_URI = "svn://guitartune.googlecode.com/svn;module=trunk;proto=http"
S = "${WORKDIR}/trunk"

inherit autotools

do_install_append() {
        install -d "${D}/${datadir}/pixmaps"
        install -m 0644 "${S}/resources/guitartune.png" "${D}/${datadir}/pixmaps"
        install -d "${D}/${datadir}/applications"
        install -m 0644 "${S}/resources/guitartune.desktop" "${D}/${datadir}/applications"
        install -d "${D}/${datadir}/guitartune"
        for ico in "${S}/resources/"*.png; do
                if [ "$(basename $ico)" != "guitartune.png" ]; then
                        install -m 0644 $ico "${D}/${datadir}/guitartune"
                fi
        done
}

FILES_${PN} += "/usr/share/guitartune/* /usr/share/applications/* /usr/share/pixmaps/*"
