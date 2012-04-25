DESCRIPTION = "EstarDict is a offline dictionary reader"
HOMEPAGE = "http://www.vaudano.eu/wiki/en/estardict"
AUTHOR = "Luca Vaudano"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary"

SRC_URI = "http://www.vaudano.eu/projects/${PN}/latest/${P}.tar.gz"
SRC_URI[md5sum] = "5b2e1e4f0578250068d8711612d0288e"
SRC_URI[sha256sum] = "463e29471013ea4db5f55475926343e7e51267dfc7a43917cf5e30b85a86ab5f"

# optimize UI
EXTRA_OECONF_append_armv4 = " --with-device=GTA"
EXTRA_OECONF_append_armv7a = " --with-device=N900"

inherit gettext autotools
