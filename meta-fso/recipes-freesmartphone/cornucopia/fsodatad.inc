require cornucopia-base.inc

inherit fso-plugin
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=78aab3f7875ffe21aebed9932fa3f993"

PE = "1"

DEPENDS += "\
  libxml2 \
  mobile-broadband-provider-info \
"
RDEPENDS_${PN} += "mobile-broadband-provider-info"
