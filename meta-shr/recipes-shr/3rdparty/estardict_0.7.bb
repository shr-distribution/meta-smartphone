DESCRIPTION = "EstarDict is a offline dictionary reader"
HOMEPAGE = "http://www.vaudano.eu/wiki/en/estardict"
AUTHOR = "Luca Vaudano"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=8f0e2cd40e05189ec81232da84bd6e1a"
SECTION = "e/apps"
DEPENDS = "elementary"

SRC_URI = "http://www.vaudano.eu/projects/${PN}/latest/${P}.tar.gz"
SRC_URI[md5sum] = "392b391fe7c34231cb64c517c7efcd24"
SRC_URI[sha256sum] = "aabbfa2e3859d44c495f15b61f93b9050ee78641f8c3e5fb955f6447ba83e07a"

# optimize UI
EXTRA_OECONF_append_armv4 = " --with-device=GTA"
EXTRA_OECONF_append_armv7a = " --with-device=N900"

inherit gettext autotools-brokensep

FILES_${PN} += "/opt/${PN}"

PNBLACKLIST[estardict] ?= "Depends on blacklisted elementary - the recipe will be removed on 2017-09-01 unless the issue is fixed"
