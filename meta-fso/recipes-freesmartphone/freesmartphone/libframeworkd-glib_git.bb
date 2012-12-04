DESCRIPTION = "freesmartphone.org API glib wrapper"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=801f80980d171dd6425610833a22dbe6"
SECTION = "devel"
DEPENDS = "dbus-glib"
SRCREV = "89b3e60b4cd393ad3104571f24f14f82917b67b5"
PV = "0.0.1+gitr${SRCPV}"
PE = "1"
PR = "r0"

SRC_URI = "${FREESMARTPHONE_GIT}/libframeworkd-glib.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

inherit autotools pkgconfig

