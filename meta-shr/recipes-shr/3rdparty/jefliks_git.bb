DESCRIPTION = "This project is a try to write real tiny and fast XMPP/Jabber \
    client for handheld devices, supported by Enlightment Foundation Library.\
    It use Elementary widget toolkit and Iksemel library to make it possible."
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
DEPENDS = "edje-native elementary iksemel gnutls eet evas ecore eina"
HOMEPAGE = "http://jefliks.sourceforge.net/"
AUTHOR = "Phoenix Kayo <kayo.k11.4@gmail.com>"
PV = "0.0.3+gitr${SRCPV}"

PNBLACKLIST[jefliks] ?= "depends on blacklisted iksemel"

inherit autotools-brokensep pkgconfig

SRCREV = "0125d93bbe9b2e527ed8dcd97332ddafb6ab360c"

SRC_URI = "git://github.com/shr-project/jefliks.git;protocol=git;branch=master \
"
S = "${WORKDIR}/git"
