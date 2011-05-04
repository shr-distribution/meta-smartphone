DESCRIPTION = "GLib-based DBus bindings for shr-project.org"
AUTHOR = "Klaus 'mrmoku' Kurzmann"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fad9b3332be894bab9bc501572864b29"
SECTION = "devel"
DEPENDS = "glib-2.0-native glib-2.0 shr-specs"
SRCREV = "761dea6463326714c00342cb53ba28a1c2dfea63"
PV = "2011.01.04.1+gitr${SRCPV}"
PR = "r0"

inherit autotools

SRC_URI = "git://git.shr-project.org/repo/libshr-glib.git;protocol=http;branch=master \
           file://0001-regen-with-gdbus-codegen-from-glib-master-repo-2.29..patch \
	   file://0002-create-old-shr-bindings.h-for-compatibility-with-old.patch"

S = "${WORKDIR}/git"

do_configure_prepend() {
       sed -i -e s#SHR_SPECS_DIR="#SHR_SPECS_DIR="${STAGING_DIR_HOST}/#g ${S}/configure.ac
}
