DESCRIPTION = "GLib-based DBus bindings for freesmartphone.org - Vala implementation"
AUTHOR = "Didier 'Ptitjes"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
SECTION = "devel"
DEPENDS = "vala-dbus-binding-tool-native glib-2.0 fso-specs"
SRCREV = "d563de6a89ef68224e293c292b7ef3b283ea5a19"
PV = "2011.01.13.1+gitr${SRCPV}"
PE = "1"
PR = "r1"

inherit autotools vala

SRC_URI = "${FREESMARTPHONE_GIT}/libfso-glib.git;protocol=git;branch=gdbus"
S = "${WORKDIR}/git"

do_configure_prepend() {
       sed -i -e s#FSO_SPECS_DIR="#FSO_SPECS_DIR="${STAGING_DIR_HOST}/#g ${S}/configure.ac
}
