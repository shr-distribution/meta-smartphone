require osmocom.inc
DESCRIPTION = "osmocombb's host tools"
HOMEPAGE = "http://bb.osmocom.org"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

SRCREV = "${OSMOCOM_SRCREV}"

PR = "${INC_PR}.0"

DEPENDS = "libosmocore"
RRECOMMENDS_${PN} = "socat"
inherit autotools-brokensep pkgconfig

S = "${WORKDIR}/git/src/host/osmocon"
