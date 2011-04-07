require msmcomm.inc

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"

PR = "${INC_PR}.1"
PV = "0.6.0+gitr${SRCPV}"
PE = "1"

DEPENDS = " \
 vala-native \
 glib-2.0 \
"

S = "${WORKDIR}/git/msmcomm-specs"

inherit autotools vala

