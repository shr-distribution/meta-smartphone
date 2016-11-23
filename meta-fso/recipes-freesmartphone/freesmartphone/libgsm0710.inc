DESCRIPTION = "A GSM 07.10 Protocol Engine"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"
SECTION = "devel"
PE = "1"

SHRT_VER = "${@d.getVar('PV', True).split('.')[0]}.${@d.getVar('PV', True).split('.')[1]}"
SRC_URI = "http://downloads.freesmartphone.org/sources/${BPN}/${SHRT_VER}/${PN}-${PV}.tar.bz2"

inherit autotools vala
