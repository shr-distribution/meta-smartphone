DESCRIPTION = "Iliwi connects you to WiFi networks"
HOMEPAGE = "http://github.com/shr-project/Iliwi"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
AUTHOR = "Esben Damgaard <ebbe@hvemder.dk>"
DEPENDS = "dbus-glib elementary libgee libeflvala"
# killall and udhcpc from busybox are enough
RDEPENDS_${PN} = "wireless-tools wpa-supplicant"

SRCREV = "0a63da80cc0e6b1644677d389074fd77f6277cc2"
PV = "0.0.1.1+gitr${SRCPV}"
PR = "r14"

SRC_URI = "git://github.com/shr-project/Iliwi.git;protocol=http;branch=master \
"

S = "${WORKDIR}/git"

inherit autotools vala
