require osmocom.inc
DESCRIPTION = "osmocombb's host tools"
HOMEPAGE = "http://bb.osmocom.org"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

SRCREV = "${OSMOCOM_SRCREV}"
SRC_URI = "git://git.osmocom.org/osmocom-bb;protocol=git;branch=master"

PR = "${INC_PR}.0"

DEPENDS = "libosmocore"
RRECOMMENDS_${PN} = "socat"
inherit autotools

S = "${WORKDIR}/git/src/host/osmocon"
