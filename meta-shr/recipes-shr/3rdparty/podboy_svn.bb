DESCRIPTION = "A podcast aggregator/player"
HOMEPAGE = "http://code.google.com/p/podboy/"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://podboy/podboy.py;endline=21;md5=d3ea8c119708d059bcc249455b330fb6"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "x11/applications"
PRIORITY = "optional"

SRCREV = "210"
PV = "1.7.2+svnr${SRCPV}"

SRC_URI = "svn://podboy.googlecode.com/svn;module=trunk;proto=http"
S = "${WORKDIR}/trunk"

inherit distutils

FILES_${PN} += "${datadir}/podboy ${datadir}/applications/podboy.desktop ${datadir}/pixmaps/podboy.png"

DEPENDS = "edje-native ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly', d)}"
RDEPENDS_${PN} += "python-compression python-elementary python-gst python-html python-netclient python-netserver python-sqlite3 python-subprocess gst-plugins-base-alsa gst-plugins-base-audioconvert gst-plugins-base-audioresample gst-plugin-bluetooth ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly-mad', d)} gst-plugins-base-ogg gst-plugins-base-volume gst-plugins-base-vorbis"

do_compile_prepend() {
	${STAGING_BINDIR_NATIVE}/edje_cc -id ${S}/data ${S}/data/podboy.edc
}
