DESCRIPTION = "freesmartphone.org API glib wrapper"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=801f80980d171dd6425610833a22dbe6"
SECTION = "devel"
DEPENDS = "dbus-glib"
SRCREV = "d71036545435ea2e72a48fb93fd1d38fc6d6c0a7"
PV = "0.0.1+gitr${SRCPV}"
PE = "1"
PR = "r0"

SRC_URI = "git://github.com/freesmartphone/libframeworkd-glib;protocol=https;branch=master"
S = "${WORKDIR}/git"

inherit autotools pkgconfig

