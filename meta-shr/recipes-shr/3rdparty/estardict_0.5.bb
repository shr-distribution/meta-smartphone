DESCRIPTION = "EstarDict is a offline dictionary reader"
HOMEPAGE = "http://www.vaudano.eu/wiki/en/estardict"
AUTHOR = "Luca Vaudano"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary"

PR = "r4"

SRC_URI = "http://www.vaudano.eu/projects/${PN}/latest/${P}.tar.gz \
  file://adapt.to.elm_item.changes.patch \
  file://adapt.to.genlist.patch \
"
SRC_URI[md5sum] = "9444263d56431b3e3f640620092304dc"
SRC_URI[sha256sum] = "e09f5af18072c8a0815d5ca35ad3db84b819416ec42ac239ec7dc8cf7a49e64b"

inherit gettext autotools
