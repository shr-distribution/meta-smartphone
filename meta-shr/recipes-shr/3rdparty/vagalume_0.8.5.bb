DESCRIPTION = "Last.fm client"
AUTHOR = "agarcia@igalia.com"
HOMEPAGE = "http://vagalume.igalia.com/"
SECTION = "x11"
DEPENDS = "gtk+ gstreamer curl gst-plugins-good gst-plugins-base ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly', d)} libnotify intltool-native"
RDEPENDS_${PN} = "curl gst-plugins-good-autodetect gst-plugins-base-audioconvert gst-plugins-base-alsa gst-plugins-good-gconfelements librsvg-gtk ${@base_conditional('ENTERPRISE_DISTRO', '1', '', 'gst-plugins-ugly-mad', d)}"
RRECOMMENDS_${PN} = "dbus-x11 hicolor-icon-theme"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "http://vagalume.igalia.com/files/source/${P}.tar.gz\
           file://index.theme \
          "
SRC_URI[md5sum] = "fba804c44ecb441bca92f499be9377e1"
SRC_URI[sha256sum] = "05210d308c686f62bb0c7a2a52b13489cb7701459d9b5b219540edb1cc27c062"

inherit autotools-brokensep pkgconfig

PACKAGECONFIG ??= ""
PACKAGECONFIG[notify] = "--enable-tray-icon,--disable-tray-icon,libnotify"
PACKAGECONFIG[libproxy] = "--enable-libproxy,--disable-libproxy,libproxy"

FILES_${PN} += "${datadir}/icons ${datadir}/dbus-1"

do_install_append() {
    install -m 0644 ${WORKDIR}/index.theme ${D}${datadir}/vagalume/icons/hicolor
}

PNBLACKLIST[vagalume] ?= "Depends on blacklisted gstreamer - the recipe will be removed on 2017-09-01 unless the issue is fixed"
