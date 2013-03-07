DESCRIPTION = "GLib-based DBus bindings for freesmartphone.org - Vala implementation"
AUTHOR = "Didier 'Ptitjes"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
SECTION = "devel"
DEPENDS = "vala-dbus-binding-tool-native vala-dbus-binding-tool glib-2.0 fso-specs"
PE = "1"

SRC_URI = "http://downloads.freesmartphone.org/sources/cornucopia/0.12/${P}.tar.bz2"

inherit autotools vala

do_configure_prepend() {
       sed -i -e s#FSO_SPECS_DIR="#FSO_SPECS_DIR="${STAGING_DIR_HOST}/#g ${S}/configure.ac
}
