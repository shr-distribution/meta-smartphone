DESCRIPTION = "A GSM 07.10 Protocol Engine"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SECTION = "devel"
SRCREV = "37ae2a2d27487ae20f0742d9044242d161df3741"
PV = "1.2.0+gitr${SRCPV}"
PE = "1"
PR = "r0"

SRC_URI = "${FREESMARTPHONE_GIT}/libgsm0710.git;protocol=git;branch=master"
S = "${WORKDIR}/git"

inherit autotools vala
