DESCRIPTIONS = "freesmartphone.org DBus API files"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
HOMEPAGE = "http://docs.freesmartphone.org"
LICENSE = "CC-BY-SA-3.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=ebef999b5d8aea38d9eb30772557f175"
DEPENDS = "libxslt-native"
SECTION = "devel/specifications"
PE = "1"

SRCNAME = "specs"

SRC_URI = "http://downloads.freesmartphone.org/sources/${SRCNAME}/${SRCNAME}-${PV}.tar.bz2"

S = "${WORKDIR}/${SRCNAME}-${PV}"

inherit autotools pkgconfig pythonnative

FILES_${PN}-dev += "${datadir}/freesmartphone/xml"
