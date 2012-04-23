DESCRIPTION = "Low level protocol implementation for binary protocol spoken by some Qualcomm modems"
HOMEPAGE = "http://www.freesmartphone.org"
AUTHOR = "Simon Busch <morphis@gravedo.de>"
SECTION = "console/network"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=59530bdf33659b29e73d4adb9f9f6552"

PE = "1"
INC_PR = "r0"

SHRT_VER = "${@bb.data.getVar('PV',d,1).split('.')[0]}.${@bb.data.getVar('PV',d,1).split('.')[1]}"
SRC_URI = "http://downloads.freesmartphone.org/sources/msmcomm/${SHRT_VER}/${P}.tar.bz2"
