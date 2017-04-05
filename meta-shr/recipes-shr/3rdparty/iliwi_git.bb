DESCRIPTION = "Iliwi connects you to WiFi networks"
HOMEPAGE = "http://github.com/shr-project/Iliwi"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
AUTHOR = "Esben Damgaard <ebbe@hvemder.dk>"
DEPENDS = "dbus-glib elementary libgee libeflvala"
# killall and udhcpc from busybox are enough
RDEPENDS_${PN} = "wireless-tools wpa-supplicant"

SRCREV = "be1ebf0978deb3b16f69e65af225b2630128e612"
PV = "0.0.1.1+gitr${SRCPV}"
PR = "r1"

PNBLACKLIST[iliwi] = "Doesn't build after vala upgrade from 0.24.0 to 0.26.1 - the recipe will be removed on 2017-09-01 unless the issue is fixed"

SRC_URI = "git://github.com/shr-project/Iliwi.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools vala
