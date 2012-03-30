DESCRIPTION = "EstarDict is a offline dictionary reader"
HOMEPAGE = "http://www.vaudano.eu/wiki/en/estardict"
AUTHOR = "Luca Vaudano"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary"
PR = "r1"

SRC_URI = "http://www.vaudano.eu/projects/${PN}/latest/${P}.tar.gz"
SRC_URI[md5sum] = "80b0c22655ef4a7c945b8c061458bd9b"
SRC_URI[sha256sum] = "d687e3beea6ea91254eefb2b3fc35d8b2020ccea3ee0c8e3f0fc8e1162f2d071"

# optimize UI
EXTRA_OECONF_append_armv4 = " --with-device=GTA"
EXTRA_OECONF_append_armv7a = " --with-device=N900"

inherit gettext autotools
