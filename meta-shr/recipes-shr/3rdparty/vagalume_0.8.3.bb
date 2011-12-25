DESCRIPTION = "Last.fm client"
AUTHOR = "agarcia@igalia.com"
HOMEPAGE = "http://vagalume.igalia.com/"
SECTION = "x11"
DEPENDS = "gtk+ gstreamer curl gst-plugins-base ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly', d)}"
RDEPENDS_${PN} = "curl gst-plugins-base-autodetect gst-plugins-base-audioconvert gst-plugins-base-alsa gst-plugins-base-gconfelements librsvg-gtk ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly-mad', d)}"
RRECOMMENDS_${PN} = "dbus-x11 hicolor-icon-theme"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
PR = "r1"

SRC_URI = "http://vagalume.igalia.com/files/source/vagalume_${PV}.orig.tar.gz\
	   file://index.theme \
	  "
S = "${WORKDIR}/${P}.orig"
inherit autotools

FILES_${PN} += "${datadir}/icons ${datadir}/dbus-1"

do_install_append() {
	install -m 0644 ${WORKDIR}/index.theme ${D}${datadir}/vagalume/icons/hicolor
}

SRC_URI[md5sum] = "f6cb301fe4eec68877484b258045d0df"
SRC_URI[sha256sum] = "aedf9efef06cd542168c0316d7696d6ba64e78d689212693264f4c5d17663f4d"
