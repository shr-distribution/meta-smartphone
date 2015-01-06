require osmocom.inc
HOMEPAGE = "http://bb.osmocom.org"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

SRCREV = "${OSMOCOM_SRCREV}"

PR = "${INC_PR}.0"

inherit autotools

S = "${WORKDIR}/git/src/shared/libosmocore"
