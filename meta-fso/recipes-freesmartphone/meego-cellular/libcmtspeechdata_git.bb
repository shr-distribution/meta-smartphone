DESCRIPTION = "Library for cellular speech data path on Nokia N900"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=249f37701ec2216105ccd3fc361f6543"
DEPENDS = "check dbus"

SRC_URI = "git://git.gitorious.org/meego-cellular/libcmtspeechdata.git;protocol=http"
SRCREV = "845034568692d289573e18163d497fce6177cffb"
PV = "0.0.0+gitr${SRCPV}"

S = "${WORKDIR}/git"

inherit autotools
